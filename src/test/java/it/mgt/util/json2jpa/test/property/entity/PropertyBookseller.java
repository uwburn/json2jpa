package it.mgt.util.json2jpa.test.property.entity;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import it.mgt.util.json2jpa.JpaIdSerializer;
import it.mgt.util.json2jpa.test.view.Skip;

import javax.persistence.*;

@Entity
@NamedQueries({
        @NamedQuery(name = "PropertyBookseller.findAll", query = "SELECT s FROM PropertyBookseller s"),
        @NamedQuery(name = "PropertyBookseller.findByName", query = "SELECT s FROM PropertyBookseller s WHERE s.firstName = :firstName AND s.lastName = :lastName")
})
public class PropertyBookseller {

    // Fields

    private Long id;
    private String firstName;
    private String lastName;
    private PropertyBookstore directedBookstore;
    private PropertyBookstore employingBookstore;


    // Constructors

    public PropertyBookseller() {
    }

    public PropertyBookseller(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Methods


    // Accessors

    @Id
    @GeneratedValue
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

    @OneToOne(mappedBy = "director")
    @JsonSerialize(using = JpaIdSerializer.class)
    @JsonView(Skip.class)
    public PropertyBookstore getDirectedBookstore() {
        return directedBookstore;
    }

    public void setDirectedBookstore(PropertyBookstore directedBookstore) {
        this.directedBookstore = directedBookstore;
    }

    @ManyToOne
    @JsonSerialize(using = JpaIdSerializer.class)
    @JsonView(Skip.class)
    public PropertyBookstore getEmployingBookstore() {
        return employingBookstore;
    }

    public void setEmployingBookstore(PropertyBookstore employingBookstore) {
        this.employingBookstore = employingBookstore;
    }
}
