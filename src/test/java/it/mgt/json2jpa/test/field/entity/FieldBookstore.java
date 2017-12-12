package it.mgt.json2jpa.test.field.entity;

import it.mgt.json2jpa.RemoveOnOrphans;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@NamedQueries({
        @NamedQuery(name = "FieldBookstore.findAll", query = "SELECT s FROM FieldBookstore s"),
        @NamedQuery(name = "FieldBookstore.findByName", query = "SELECT s FROM FieldBookstore s WHERE s.name = :name")
})
public class FieldBookstore {

    // Fields

    @Id
    @GeneratedValue
    private Long id;
    private String name;
    @OneToOne
    private FieldBookseller director;
    @OneToMany(mappedBy = "employingBookstore", cascade = CascadeType.ALL)
    @RemoveOnOrphans
    private Set<FieldBookseller> employees = new LinkedHashSet<>();
    @ManyToMany
    @RemoveOnOrphans
    private Set<FieldBook> books = new LinkedHashSet<>();


    // Constructors

    public FieldBookstore() {
    }

    public FieldBookstore(String name) {
        this.name = name;
    }

    // Methods


    // Accessors

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

    public FieldBookseller getDirector() {
        return director;
    }

    public void setDirector(FieldBookseller director) {
        this.director = director;
    }

    public Set<FieldBookseller> getEmployees() {
        return employees;
    }

    public void setEmployees(Set<FieldBookseller> employees) {
        this.employees = employees;
    }

    public Set<FieldBook> getBooks() {
        return books;
    }

    public void setBooks(Set<FieldBook> books) {
        this.books = books;
    }
}
