package it.mgt.util.json2jpa.test.subtypes.entity;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
@DiscriminatorValue("student")
@NamedQueries({
        @NamedQuery(name = "Student.findBySsn", query = "SELECT s FROM Student s WHERE s.ssn = :ssn")
})
public class Student extends Person {

    // Fields

    private String section;


    // Constructors

    public Student() {
    }

    public Student(String firstName, String lastName, String ssn, Party party, String section) {
        super(firstName, lastName, ssn, party);
        this.section = section;
    }


    // Accessors

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }
}
