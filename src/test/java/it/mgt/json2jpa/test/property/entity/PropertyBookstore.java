package it.mgt.json2jpa.test.property.entity;

import it.mgt.json2jpa.RemoveOnOrphans;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@NamedQueries({
        @NamedQuery(name = "PropertyBookstore.findAll", query = "SELECT s FROM PropertyBookstore s"),
        @NamedQuery(name = "PropertyBookstore.findByName", query = "SELECT s FROM PropertyBookstore s WHERE s.name = :name")
})
public class PropertyBookstore {

    // Fields

    private Long id;
    private String name;
    private PropertyBookseller director;
    private Set<PropertyBookseller> employees = new LinkedHashSet<>();
    private Set<PropertyBook> books = new LinkedHashSet<>();


    // Constructors

    public PropertyBookstore() {
    }

    public PropertyBookstore(String name) {
        this.name = name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OneToOne
    public PropertyBookseller getDirector() {
        return director;
    }

    public void setDirector(PropertyBookseller director) {
        this.director = director;
    }

    @OneToMany(mappedBy = "employingBookstore", cascade = CascadeType.ALL)
    @RemoveOnOrphans
    public Set<PropertyBookseller> getEmployees() {
        return employees;
    }

    public void setEmployees(Set<PropertyBookseller> employees) {
        this.employees = employees;
    }

    @ManyToMany
    @RemoveOnOrphans
    public Set<PropertyBook> getBooks() {
        return books;
    }

    public void setBooks(Set<PropertyBook> books) {
        this.books = books;
    }
}
