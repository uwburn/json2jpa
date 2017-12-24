package it.mgt.jpa.json2jpa.test.field.entity;

import javax.persistence.*;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@NamedQuery(name="FieldRole.findByName", query="SELECT r FROM FieldRole r WHERE r.name = :name")
public class FieldRole {

    // Fields

    @Id
    @GeneratedValue
    private Long id;
    private String name;
    @ManyToOne
    private FieldCompany company;
    @ElementCollection(targetClass = FieldOperation.class)
    @Enumerated(EnumType.STRING)
    private Set<FieldOperation> operations = new LinkedHashSet<>();
    @ManyToMany(mappedBy = "roles")
    private Set<FieldEmployee> employees = new LinkedHashSet<>();


    // Constructors

    public FieldRole() {
    }

    public FieldRole(String name, FieldCompany company, FieldOperation... operations) {
        this.company = company;
        this.name = name;
        this.operations = Arrays.stream(operations)
                .collect(Collectors.toSet());
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

    public Set<FieldOperation> getOperations() {
        return operations;
    }

    public void setOperations(Set<FieldOperation> operations) {
        this.operations = operations;
    }

    public Set<FieldEmployee> getEmployees() {
        return employees;
    }

    public void setEmployees(Set<FieldEmployee> employees) {
        this.employees = employees;
    }

    public FieldCompany getCompany() {
        return company;
    }

    public void setCompany(FieldCompany company) {
        this.company = company;
    }

}