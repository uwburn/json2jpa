package it.mgt.util.json2jpa.test.hooks.entity;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
public class Country {

    @Id
    @GeneratedValue
    protected Long id;
    protected String name;
    protected String countryCode;
    protected String phonePrefix;
    @OneToOne
    protected PersonH president;
    @OneToMany(mappedBy = "country")
    protected Set<Region> regions = new LinkedHashSet<>();
    @ManyToMany(mappedBy = "visitedCountries")
    protected Set<PersonH> visitors = new LinkedHashSet<>();

    public Country() {
    }

    public Country(String name, String countryCode, String phonePrefix) {
        this.name = name;
        this.countryCode = countryCode;
        this.phonePrefix = phonePrefix;
    }

    public Country(String name, String countryCode, String phonePrefix, PersonH president) {
        this(name, countryCode, phonePrefix);
        this.president = president;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Country)) return false;

        Country country = (Country) o;

        return getId() != null ? getId().equals(country.getId()) : country.getId() == null;
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

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public Set<Region> getRegions() {
        return regions;
    }

    public void setRegions(Set<Region> regions) {
        this.regions = regions;
    }

    public String getPhonePrefix() {
        return phonePrefix;
    }

    public void setPhonePrefix(String phonePrefix) {
        this.phonePrefix = phonePrefix;
    }

    public PersonH getPresident() {
        return president;
    }

    public void setPresident(PersonH president) {
        this.president = president;
    }

    public Set<PersonH> getVisitors() {
        return visitors;
    }

    public void setVisitors(Set<PersonH> visitors) {
        this.visitors = visitors;
    }
}
