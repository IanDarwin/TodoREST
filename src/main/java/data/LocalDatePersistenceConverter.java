package com.ticketmanor.model.jpa;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/** JPA Converter for LocalDate, remove when JPA catches up with Java 8
 * @author Steven Gertiser, https://weblogs.java.net/blog/montanajava/archive/2014/06/17/using-java-8-datetime-classes-jpa
 */
@Converter(autoApply = true)
public class LocalDatePersistenceConverter
	implements AttributeConverter<LocalDate, Timestamp> {
	
    @Override
    public java.sql.Timestamp convertToDatabaseColumn(LocalDate entityValue) {
        return Timestamp.valueOf(LocalDateTime.of(entityValue, LocalTime.of(0,0)));
    }

    @Override
    public LocalDate convertToEntityAttribute(java.sql.Timestamp databaseValue) {
        return databaseValue.toLocalDateTime().toLocalDate();
    }
}
