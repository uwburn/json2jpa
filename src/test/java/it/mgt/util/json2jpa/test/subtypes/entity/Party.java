package it.mgt.util.json2jpa.test.subtypes.entity;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@NamedQueries({
        @NamedQuery(name = "Party.findByName", query = "SELECT p FROM Party p WHERE p.name = :name")
})
public class Party {

    // Fields

    @Id
    @GeneratedValue
    private Long id;
    private String name;
    @OneToMany(mappedBy = "party")
    private Set<Person> people = new LinkedHashSet<>();


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

    public Set<Person> getPeople() {
        return people;
    }

    public void setPeople(Set<Person> people) {
        this.people = people;
    }
}
