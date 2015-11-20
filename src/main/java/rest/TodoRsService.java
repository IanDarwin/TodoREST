package rest;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.darwinsys.todo.model.Task;

import data.TodoList;

@Path("/todo")
public class TodoRsService {

	@Inject
	TodoList lister;
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String sayHello() {
		return "<p><b>Hello!</b></p>";
	}
	@GET
	@Path("/list")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Task> doList() {
		System.out.println("TodoRsService.doList()");
		return lister.findAll();
	}
	
	@POST
	@Path("/updt")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doUpdate(Task t) {
		return Response.notModified().entity("Not written yet").build();
	}
}
