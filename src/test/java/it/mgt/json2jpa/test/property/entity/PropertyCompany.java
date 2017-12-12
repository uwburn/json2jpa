package it.mgt.json2jpa.test.property.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@DiscriminatorValue("COMPANY")
@NamedQueries({
        @NamedQuery(name="PropertyCompany.findAll", query="SELECT c FROM PropertyCompany c"),
        @NamedQuery(name="PropertyCompany.findByName", query="SELECT c FROM PropertyCompany c WHERE c.name = :name")
})
@JsonIgnoreProperties({"anthem"})
public class PropertyCompany extends PropertyOrganization {

    // Fields

    private String motto;
    private String anthem;
    private Set<PropertyEmployee> employees = new LinkedHashSet<>();
    private Set<PropertyRole> roles = new LinkedHashSet<>();


    // Constructors

    public PropertyCompany() {

    }

    public PropertyCompany(String companyName, String address, String motto, String anthem) {
        super(companyName, address);
        this.motto = motto;
        this.anthem = anthem;
    }

    // Methods


    // Accessors

    @JsonIgnore
    public String getMotto() {
        return motto;
    }

    public void setMotto(String motto) {
        this.motto = motto;
    }

    public String getAnthem() {
        return anthem;
    }

    public void setAnthem(String anthem) {
        this.anthem = anthem;
    }

    @OneToMany(mappedBy = "company", cascade = CascadeType.PERSIST)
    public Set<PropertyEmployee> getEmployees() {
        return employees;
    }

    public void setEmployees(Set<PropertyEmployee> employees) {
        this.employees = employees;
    }

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    public Set<PropertyRole> getRoles() {
        return roles;
    }

    public void setRoles(Set<PropertyRole> roles) {
        this.roles = roles;
    }

}