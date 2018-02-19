package it.mgt.util.json2jpa.test.subtypes.entity;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import java.util.Date;

@Entity
@DiscriminatorValue("employee")
@NamedQueries({
        @NamedQuery(name = "Employee.findBySsn", query = "SELECT e FROM Employee e WHERE e.ssn = :ssn")
})
public class Employee extends Person {

    // Fields

    private Date hiringDate;


    // Constructors

    public Employee() {
    }

    public Employee(String firstName, String lastName, String ssn, Party party, Date hiringDate) {
        super(firstName, lastName, ssn, party);
        this.hiringDate = hiringDate;
    }


    // Accessors

    public Date getHiringDate() {
        return hiringDate;
    }

    public void setHiringDate(Date hiringDate) {
        this.hiringDate = hiringDate;
    }
}
