package it.mgt.util.json2jpa.test.property.entity;

import com.fasterxml.jackson.annotation.JsonView;
import it.mgt.util.json2jpa.test.view.Skip;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@NamedQueries({
        @NamedQuery(name = "PropertyBook.findAll", query = "SELECT b FROM PropertyBook b"),
        @NamedQuery(name = "PropertyBook.findByIsbn", query = "SELECT b FROM PropertyBook b WHERE b.isbn = :isbn")
})
public class PropertyBook {

    // Fields

    private Long id;
    private String title;
    private String isbn;
    private double price;
    private Set<PropertyBookstore> bookstores = new LinkedHashSet<>();


    // Constructors

    public PropertyBook() {
    }

    public PropertyBook(String title, String isbn, double price) {
        this.title = title;
        this.isbn = isbn;
        this.price = price;
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

    @ManyToMany
    @OrderBy("id ASC")
    @JsonView(Skip.class)
    public Set<PropertyBookstore> getBookstores() {
        return bookstores;
    }

    public void setBookstores(Set<PropertyBookstore> bookstores) {
        this.bookstores = bookstores;
    }
}
