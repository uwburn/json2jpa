package it.mgt.util.json2jpa.test.property.entity;

import javax.persistence.*;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@NamedQuery(name="PropertyRole.findByName", query="SELECT r FROM PropertyRole r WHERE r.name = :name")
public class PropertyRole {

    // Fields

    private Long id;
    private String name;
    private PropertyCompany company;
    private Set<PropertyOperation> operations = new LinkedHashSet<>();
    private Set<PropertyEmployee> employees = new LinkedHashSet<>();


    // Constructors

    public PropertyRole() {
    }

    public PropertyRole(String name, PropertyCompany company, PropertyOperation... operations) {
        this.company = company;
        this.name = name;
        this.operations = Arrays.stream(operations)
                .collect(Collectors.toSet());
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

    @ElementCollection(targetClass = PropertyOperation.class)
    @Enumerated(EnumType.STRING)
    public Set<PropertyOperation> getOperations() {
        return operations;
    }

    public void setOperations(Set<PropertyOperation> operations) {
        this.operations = operations;
    }

    @ManyToMany(mappedBy = "roles")
    public Set<PropertyEmployee> getEmployees() {
        return employees;
    }

    public void setEmployees(Set<PropertyEmployee> employees) {
        this.employees = employees;
    }

    @ManyToOne
    public PropertyCompany getCompany() {
        return company;
    }

    public void setCompany(PropertyCompany company) {
        this.company = company;
    }

}