package rest;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("")
public class TodoRsApplication extends Application {

	public TodoRsApplication() {
		System.out.println("TodoRsApplication.TodoRsApplication()");
	}

	/** 
	 * This is an efficiency tweak - only scan for annotations in
	 * classes in this Set.
	 */
	@Override
	public Set<Class<?>> getClasses() {
		System.out.println("TodoRsApplication.getClasses()");
		Set<Class<?>> cl = new HashSet<>();
		cl.add(TodoRsService.class);
		return cl;
	}
}
