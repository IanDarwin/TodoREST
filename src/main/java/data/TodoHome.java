package data;

import java.io.Serializable;

import javax.annotation.PreDestroy;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import com.darwinsys.todo.model.Task;

import org.omg.CORBA.StringHolder;

/**
 * Implements Gateway, an Adam Bien pattern whose purpose is to expose
 * an Entity (and its relations) to the Client/Web tier, rather like a Seam2 "Home Object"
 * @author Ian Darwin
 */
@Stateful @Named @ConversationScoped
public class TodoHome implements Serializable {

	private static final long serialVersionUID = -2284578724132631798L;

	@PersistenceContext(type=PersistenceContextType.EXTENDED) EntityManager em;

	private static final String LIST_PAGE = "TodoList";
	private static final String FORCE_REDIRECT = "?faces-redirect=true";

	@Inject Conversation conv;

	// Must be Long (not long) so we can check for null
	private Long id;
	private Task instance = newInstance();

	public Task newInstance() {
		System.out.println("TaskHome.newInstance()");
		return new Task();
	}

	public TodoHome() {
		System.out.println("TodoHome.TodoHome()");
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		System.out.println("TodoHome.setId(" + id + ")");
		this.id = id;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void wire() {
		System.out.println("Wire(): " + id);
		if (conv.isTransient()) {
			conv.begin();
		}
		if (id == null) {
			instance = new Task();
			return;
		}
		instance = em.find(Task.class, id);
		if (instance == null) {
			throw new IllegalArgumentException("Todo not found by id! " + id);
		}
	}
	public void wire(Long id) {
		System.out.println("TodoHome.wire(" + id + ")");
		setId(id);
		wire();
	}

	/** The C of CRUD - create a new T in the database */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public String save() {
		System.out.println("TodoHome.save()");
		em.persist(instance);
		conv.end();
		return LIST_PAGE + FORCE_REDIRECT;
	}
	
	/** The R of CRUD - Download a T by primary key
	 * @param id The primary key of the entity to find
	 * @return The found entity
	 */
	public Task find(long id) {		
		return em.find(Task.class, id);
	}

	/** The U of CRUD - update an Entity
	 * @param entity The entity to update
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public String update() {
		System.out.println("TodoHome.update()");
		em.merge(instance);
		return LIST_PAGE + FORCE_REDIRECT;
	}

	/** The D of CRUD - delete an Entity. Use with care! */
	public String remove() {
		System.out.println("TodoHome.remove()");
		em.remove(instance);
		conv.end();
		return LIST_PAGE + FORCE_REDIRECT;
	}
	
	public Task getInstance() {
		System.out.println("TodoHome.getInstance(): " + instance);
		return instance;
	}
	public void setInstance(Task instance) {
		this.instance = instance;
		// this.id = instance.getId();
	}
	
	/** Close an editing operation: just end conversation, return List page.
	 * @return The List Page
	 */
	public String cancel() {
		conv.end();
		return LIST_PAGE + FORCE_REDIRECT;
	}
	
	/** Like Cancel but for e.g., View page, no conv end.
	 * @return The List Page
	 */
	public String done() {
		return LIST_PAGE + FORCE_REDIRECT;
	}

	@PreDestroy
	public void bfn() {
		if (!conv.isTransient()) {
			conv.end();
		}
		System.out.println("TaskHome.bfn()");
	}

	/**
	 * A Remove method that is effectively stateless; it is called
	 * with a detached entity so we need to reconnect it first.
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void remove(Task victim) {
		long id = victim.getId();
		System.out.println("Removing Todo Task #" + id);
		em.remove(em.merge(victim));
	}

}
