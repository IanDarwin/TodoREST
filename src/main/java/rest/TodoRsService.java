package rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

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
		// True if we want to print/log call activity
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
	void checkAuth(String userName) {
		String remoteUser = request.getRemoteUser();
		if (userName.equals(remoteUser)) {
			return;
		}
		var mesg = debug ?
				String.format("Not Authorized - asked %s, logged in as %s",
						userName,remoteUser):
				"Not Authorized - wrong user";
		throw new WebApplicationException(mesg, Status.FORBIDDEN);
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
				LocalDateTime.now(),
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
