package data;

import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Default;
import javax.inject.Named;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.Repository;

import com.darwinsys.todo.model.Task;

/**
 * This is a basic DAO-like interface for use by JSF or EJB.
 * Methods are implemented for us by Apache DeltaSpike Data.
 * The methods in the inherited interface suffice for many apps!
 * @author Ian Darwin
 */
@Named("todoList") @Default
@SessionScoped
@Repository 
public interface TodoList extends Serializable, EntityRepository<Task, Long> {

	// The ordering of Priority is 0=Top..3=Lowest, so sort by prio asc is correct here
	@Query(value="select t from Task t where t.status < 3 order by t.priority asc, t.name asc")
	List<Task> findAll();
}
