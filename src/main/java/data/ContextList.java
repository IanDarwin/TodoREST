package data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Default;
import javax.inject.Named;

import com.darwinsys.todo.model.Context;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.Repository;

/**
 * This is a basic DAO-like interface for use by JSF or EJB.
 * Methods are implemented for us by Apache DeltaSpike Data.
 * The methods in the inherited interface suffice for many apps!
 * @author Ian Darwin
 */
@Named("contextList") @Default
@SessionScoped
@Repository 
public interface ContextList extends Serializable, EntityRepository<Context, Long> {

	@Query(value="select c from Context c order by lower(c.name) asc")
	List<Context> findAll();
}
