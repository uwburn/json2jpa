package it.mgt.util.json2jpa.test.hooks.entity;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@NamedQueries({
        @NamedQuery(name = "Region.findByName", query = "SELECT r FROM Region r WHERE r.name = :name")
})
public class Region {

    @Id
    @GeneratedValue
    protected Long id;
    protected String name;
    @ManyToOne
    protected Country country;
    @OneToMany
    protected Set<Town> towns = new LinkedHashSet<>();

    public Region() {
    }

    public Region(String name, Country country) {
        this.name = name;
        this.country = country;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Region)) return false;

        Region region = (Region) o;

        return getId() != null ? getId().equals(region.getId()) : region.getId() == null;
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

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public Set<Town> getTowns() {
        return towns;
    }

    public void setTowns(Set<Town> towns) {
        this.towns = towns;
    }
}
