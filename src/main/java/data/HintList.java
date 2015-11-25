package data;

import java.io.Serializable;
import java.util.Random;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Default;
import javax.inject.Named;
import javax.persistence.*;

import com.darwinsys.todo.model.Hint;

/**
 * This is a basic DAO-like interface for use by JSF or EJB.
 * There's one method and we need EntityManager so forget DeltaSpike Data.
 * @author Ian Darwin
 */
@Named("hintList") @Default
@SessionScoped
public class HintList implements Serializable {

	@PersistenceContext
	EntityManager em;

	public Hint getRandom() {
		int count =
			em.createQuery("select count(h) from Hint h")
			.getFirstResult();

		System.out.printf("Found %d Hint(s)%n", count);

		if (count == 0) {
			System.err.println("No hints in database");
			Hint h = new Hint();
			h.setHint("A stitch in time saves nine");
			return h;
		}
		int index = new Random().nextInt((int)count) + 1; // JPA starts at 1

		return em.createQuery("select h from Hint h", Hint.class)
			.setFirstResult(index)
			.setMaxResults(1)
			.getSingleResult();
	}
}
