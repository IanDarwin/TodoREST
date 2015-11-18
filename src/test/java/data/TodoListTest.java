package data;

import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.List;

import javax.persistence.metamodel.SingularAttribute;

import org.junit.Test;

import com.darwinsys.todo.model.Priority;
import com.darwinsys.todo.model.Task;

/**
 * ATM this tests ONLY the default method in the List interface;
 * need to move to Arquillian to test the actual list methods.
 */
public class TodoListTest {

	@Test
	public void testOrderByPrio() {
		Task t0 = new Task();
		t0.setName("t0");
		t0.setPriority(Priority.Low);
		
		Task t1 = new Task();
		t1.setName("t1");
		t1.setPriority(Priority.Top);

		List<Task> l = Arrays.asList(new Task[] { t0, t1 });

		List<Task> sorted = TodoList.orderByPriority(l);

		assertSame(t0, sorted.get(1));
		assertSame(t1, sorted.get(0));
	}
}
