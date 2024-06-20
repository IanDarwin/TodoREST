package model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import jakarta.persistence.Table;

/** A user of the system.
 * JPA Entities cannot be Java 'record' types.
 */
@Entity
@Table(name="person")
// "user' is a reserved word in some SQLs, at least Postgres
public class User {

	long id;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPasswdEncrypted() {
		return passwdEncrypted;
	}

	public void setPasswdEncrypted(String passwdEncrypted) {
		this.passwdEncrypted = passwdEncrypted;
	}

	public String username;

	public String passwdEncrypted;

	public String firstName;

	public String lastName;

	@Id
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
}
