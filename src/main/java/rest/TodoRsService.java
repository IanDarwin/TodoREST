package rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.darwinsys.todo.model.Task;

/**
 * This implements only the web-service part of the ToDo server.
 * It uses a JPA model to access the data.
 * @author Ian Darwin
 */
@Path("")
@ApplicationScoped
public class TodoRsService {
	
	@PersistenceContext
	EntityManager entityManager;
	@Context HttpServletRequest request;
	
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
	
	private boolean
		/** True if we want to print/log call activity */
		trace = true,
		debug = false;

	public static final String WEB_SERVICE_VERSION = "2.0";
	
	private Map<String, String> users = new HashMap<String,String>(100);
	
	static {
		System.out.println("ToDoServer.RestService class loaded.");
	}
	
	public TodoRsService() {
		System.out.println("ToDoServer.RestService.<init>");
	}

	/** Diagnostic printing */
	void trace(String mesg) {
		if (trace) {
			System.out.println(mesg);
		}
	}
	
	/**
	 * Called from methods that access user data.
	 * @throws WebApplicationException(403) if not authorized
	 * @param userName The name being passed in.
	 */
	public void checkAuth(String userName) {
		if (userName.equals(request.getRemoteUser())) {
			return;
		}
		throw new WebApplicationException("Not Authorized - wrong user", Status.FORBIDDEN);
	}
	
	/** This is a system-status "ping" type service */
	@GET @Path("/status")
	@Produces({MediaType.APPLICATION_JSON,MediaType.TEXT_HTML})
	public String getStatus() {
		trace("GET RestService.getStatus()");
		return String.format(
				"{" +
					"\"database_status\": \"working\", " +
					"\"web_service_version\": \"%s\", " +
					"\"time_on_server\": \"%s\", " +
					"\"options/trace\": \"%s\", " +
					"\"status\": \"running\"" + // Comma not allowed at end in standard JSON
				"}",
				WEB_SERVICE_VERSION,
				LocalDate.now(),
				trace 
		);
	}

	/** Options settings - simple for now */
	@GET @Path("/options/{option}/{value}")
	@Produces("text/plain")
	public String setOption(@PathParam("option")String option, @PathParam("value")boolean value) {
		// Do NOT use trace() here!
		if ("trace".equals(option)) {
			System.out.println("RestService.set Trace = " + value + ")");
			this.trace = value;
			return "ok; set to " + this.trace;
		} else if ("debug".equals(option)) {
			System.out.println("RestService.set debug = " + value + ")");
			this.debug = value;
			return "ok; set to " + this.debug;
		}
		return "ok; WARNING: unknown option: " + option;
	}
	
	/** Used to update or upload a ToDo item known as a "Task"
	 * @throws ParseException on certain invalid inputs
	 */
	@POST @Path("/{userName}/save")
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional(value=TxType.REQUIRED)
	public Response saveTask(Task task, @PathParam("userName") String userName) {
		
		checkAuth(userName);
		
		trace("POST /" + userName + "/task");
		
		try {
			if (task.getServerId() > 0) {
				// Changed on the device
				entityManager.merge(task);
				return Response.accepted(new URI(String.format("/%s/tasks/%d", userName, task.getServerId()))).build();
			} else {
				// created on the device
				entityManager.persist(task);
				entityManager.flush();        // before calling getId()!
				// REST theory dictates that "create" should return the URI of the new resource:
				return Response.created(new URI(String.format("/%s/tasks/%d", userName, task.getServerId()))).build();
			}
		} catch (URISyntaxException e) {
			// CANT HAPPEN
			System.err.println("UNEXPECTED ERROR: " + e);
			e.printStackTrace();
			return Response.serverError().build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.notModified("FAIL-persistence: " + e).build();
		}
	}
	
	/** Used to download all items for the given user */
	// XXX FOR ALL USERS ATM
	@GET @Path("/{userName}/task")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Task> findTasksForUser(@PathParam("userName")String userName) {
		trace("Find tasks for user " + userName);
		checkAuth(userName);
		return entityManager.createQuery("from Task t order by t.priority asc, t.name asc", Task.class).getResultList();
	}
	
	/** Used to download an item BY item ID */
	@GET @Path("/{userName}/task/{itemId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Task findTaskById(@PathParam("userName")String userName,  @PathParam("itemId")long id) {
		trace(String.format("GET tasks/item#%d", id));
		checkAuth(userName);
		return entityManager.find(Task.class, id);
	}

	/** Used (with great trepidation) by the remote to delete an entry
	 * The input is a dummy Task, whose only used field here is serverId.
	 */
	@POST @Path("/{userName}/task/smersh")
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional(value=TxType.REQUIRED)
	public Response smersh(Task task, @PathParam("userName") String userName) {
		entityManager.remove(entityManager.merge(task));
		// If we get here, it's good
		return Response.ok().build();
	}
}
