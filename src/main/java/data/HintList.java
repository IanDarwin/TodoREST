package data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Default;
import javax.inject.Named;

import com.darwinsys.todo.model.Hint;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.Repository;

/**
 * This is a basic DAO-like interface for use by JSF or EJB.
 * Methods are implemented for us by Apache DeltaSpike Data.
 * The methods in the inherited interface suffice for many apps!
 * @author Ian Darwin
 */
@Named("hintList") @Default
@SessionScoped
@Repository 
public interface HintList extends Serializable, EntityRepository<Hint, Long> {

	List<Hint> findAll();

	@Query(value="select h from Hint h order by random() limit 1")
	Hint findRandom();
}

