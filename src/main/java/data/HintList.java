package data;

import java.io.Serializable;
import java.util.List;
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

	private Random r = new Random();

	/**
	 * Design of this is from all DeltaSpike Data listers.
	 */
	public List<Hint> findAll() {
		return em.createQuery("from Hint", Hint.class).getResultList();
	}

	/**
	 * Meant to be useful on Todo pages: print one randomly-chosen Hint.
	 */
	public Hint getRandom() {
		long count =
			em.createQuery("select count(h) from Hint h", Long.class)
			.getSingleResult();

		if (count == 0) {
			System.err.println("No hints in database");
			Hint h = new Hint();
			h.setHint("A stitch in time saves nine");
			return h;
		} else {
			System.out.printf("Found %d Hint(s)%n", count);
		}
		int index = r.nextInt((int)count); // JPA starts at 1

		System.out.printf("Index = %d%n", index);

		return em.createQuery("select h from Hint h", Hint.class)
			.setFirstResult(index)
			.setMaxResults(1)
			.getSingleResult();
	}
}
