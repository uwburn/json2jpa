package it.mgt.json2jpa.test.field.entity;

import javax.persistence.*;

@Entity
@NamedQueries({
        @NamedQuery(name = "FieldBookseller.findAll", query = "SELECT s FROM FieldBookseller s"),
        @NamedQuery(name = "FieldBookseller.findByName", query = "SELECT s FROM FieldBookseller s WHERE s.firstName = :firstName AND s.lastName = :lastName")
})
public class FieldBookseller {

    // Fields

    @Id
    @GeneratedValue
    private Long id;
    private String firstName;
    private String lastName;
    @OneToOne(mappedBy = "director")
    private FieldBookstore directedBookstore;
    @ManyToOne
    private FieldBookstore employingBookstore;


    // Constructors

    public FieldBookseller() {
    }

    public FieldBookseller(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Methods


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

    public FieldBookstore getDirectedBookstore() {
        return directedBookstore;
    }

    public void setDirectedBookstore(FieldBookstore directedBookstore) {
        this.directedBookstore = directedBookstore;
    }

    public FieldBookstore getEmployingBookstore() {
        return employingBookstore;
    }

    public void setEmployingBookstore(FieldBookstore employingBookstore) {
        this.employingBookstore = employingBookstore;
    }
}
