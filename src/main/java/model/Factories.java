package model;

import java.util.Arrays;
import java.util.List;

import javax.ejb.Singleton;
import javax.inject.Named;

import com.darwinsys.todo.model.Priority;
import com.darwinsys.todo.model.Status;

@Singleton
@Named
public class Factories {

	public Priority[] getPriorities() {
		return Priority.values();
	}
	public Status[] getStatuses() {
		return Status.values();
	}
}
