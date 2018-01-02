package it.mgt.util.json2jpa.test.field.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@DiscriminatorValue("COMPANY")
@NamedQueries({
        @NamedQuery(name="FieldCompany.findAll", query="SELECT c FROM FieldCompany c"),
        @NamedQuery(name="FieldCompany.findByName", query="SELECT c FROM FieldCompany c WHERE c.name = :name")
})
@JsonIgnoreProperties({"anthem"})
public class FieldCompany extends FieldOrganization {

    // Fields

    @JsonIgnore
    private String motto;
    private String anthem;
    @OneToMany(mappedBy = "company", cascade = CascadeType.PERSIST)
    private Set<FieldEmployee> employees = new LinkedHashSet<>();
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    private Set<FieldRole> roles = new LinkedHashSet<>();


    // Constructors

    public FieldCompany() {

    }

    public FieldCompany(String companyName, String address, String motto, String anthem) {
        super(companyName, address);
        this.motto = motto;
        this.anthem = anthem;
    }

    // Methods


    // Accessors


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

    public Set<FieldEmployee> getEmployees() {
        return employees;
    }

    public void setEmployees(Set<FieldEmployee> employees) {
        this.employees = employees;
    }

    public Set<FieldRole> getRoles() {
        return roles;
    }

    public void setRoles(Set<FieldRole> roles) {
        this.roles = roles;
    }

}