package model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import javax.persistence.Table;

/** A user of the system.
 * JPA Entities cannot be Java 'record' types.
 */
@Entity
@Table(name="person")
// "user' is a reserved word in at least Postgres
public class User {

	@Id
	long id;

	public String username;

	public String passwdEncrypted;

	public String firstName;

	public String lastName;
}
