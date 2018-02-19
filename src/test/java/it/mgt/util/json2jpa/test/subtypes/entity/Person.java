package it.mgt.util.json2jpa.test.subtypes.entity;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.persistence.*;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = "ssn")
})
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("base")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(Employee.class)
})
public abstract class Person {

    // Fields

    @Id
    @GeneratedValue
    protected Long id;
    @Column(nullable = false)
    protected String firstName;
    @Column(nullable = false)
    protected String lastName;
    @Column(nullable = false)
    protected String ssn;
    @ManyToOne
    protected Party party;


    // Constructor

    public Person() {
    }

    public Person(String firstName, String lastName, String ssn, Party party) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.ssn = ssn;
        this.party = party;
    }


    // Accessors

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public Party getParty() {
        return party;
    }

    public void setParty(Party party) {
        this.party = party;
    }
}
