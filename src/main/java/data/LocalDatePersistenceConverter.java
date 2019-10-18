package data;

import java.time.LocalDate;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/** JPA Converter for LocalDate, remove when JPA catches up with Java 8
 * @author Based on one by Steven Gertiser, https://community.oracle.com/blogs/montanajava/2014/06/16/using-java-8-datetime-classes-jpa
 */
@Converter(autoApply = true)
public class LocalDatePersistenceConverter
	implements AttributeConverter<LocalDate, java.sql.Date> {
	
	@Override
    public java.sql.Date convertToDatabaseColumn(LocalDate entityValue) {
        return java.sql.Date.valueOf(entityValue);
    }

    @Override
    public LocalDate convertToEntityAttribute(java.sql.Date databaseValue) {
		return (databaseValue != null) ? databaseValue.toLocalDate() : null;
    }
}
