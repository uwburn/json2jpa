package it.mgt.util.json2jpa.test.hooks.entity;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
public class PersonH {

    @Id
    @GeneratedValue
    protected Long id;
    protected String name;
    @OneToOne(mappedBy = "president")
    protected Country countryByPresident;
    @ManyToMany
    protected Set<Country> visitedCountries = new LinkedHashSet<>();

    public PersonH() {
    }

    public PersonH(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PersonH)) return false;

        PersonH person = (PersonH) o;

        return getId() != null ? getId().equals(person.getId()) : person.getId() == null;
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }

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

    public Set<Country> getVisitedCountries() {
        return visitedCountries;
    }

    public void setVisitedCountries(Set<Country> visitedCountries) {
        this.visitedCountries = visitedCountries;
    }

    public Country getCountryByPresident() {
        return countryByPresident;
    }

    public void setCountryByPresident(Country countryByPresident) {
        this.countryByPresident = countryByPresident;
    }
}
