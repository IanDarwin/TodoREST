package data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import com.darwinsys.todo.model.Hint;
import com.darwinsys.todo.model.Task;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class PersistenceTest {

	private static EntityManagerFactory emf;
	private EntityManager em;

	final static String FIRST = "Loodlie";

	@BeforeClass
	public static void preClass() {
		emf = Persistence.createEntityManagerFactory("todolist");
	}

	@Before
	public void preTest() {
		em = emf.createEntityManager();
	}

	/** Live JPA (fake database): save a Todo Task, then
	 * assert that we find it in the list from JPA
	 */
	@Test
	public void testLoadStore() {
		Task tm = new Task();
		tm.setName(FIRST);

		em.getTransaction().begin();
		em.persist(tm);
		em.getTransaction().commit();

		List<Task> ms = em.createQuery("from Task", Task.class).getResultList();
		assertTrue("found any", ms.size() > 0);

		for (Task m : ms) {
			if (m.getName().equals(FIRST)) {
				return;
			}
		}
		fail("Did not retrieve datum that we saved!");
	}
}
