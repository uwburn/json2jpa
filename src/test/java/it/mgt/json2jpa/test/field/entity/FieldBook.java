package it.mgt.json2jpa.test.field.entity;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@NamedQueries({
        @NamedQuery(name = "FieldBook.findAll", query = "SELECT b FROM FieldBook b"),
        @NamedQuery(name = "FieldBook.findByIsbn", query = "SELECT b FROM FieldBook b WHERE b.isbn = :isbn")
})
public class FieldBook {

    // Fields

    @Id
    @GeneratedValue
    private Long id;
    private String title;
    private String isbn;
    private double price;
    @ManyToMany
    private Set<FieldBookstore> bookstores = new LinkedHashSet<>();


    // Constructors

    public FieldBook() {
    }

    public FieldBook(String title, String isbn, double price) {
        this.title = title;
        this.isbn = isbn;
        this.price = price;
    }


    // Methods


    // Accessors

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Set<FieldBookstore> getBookstores() {
        return bookstores;
    }

    public void setBookstores(Set<FieldBookstore> bookstores) {
        this.bookstores = bookstores;
    }
}
