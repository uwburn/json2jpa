package it.mgt.json2jpa.test.field.entity;

import com.fasterxml.jackson.annotation.JsonView;
import it.mgt.json2jpa.test.view.NoSsn;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = "ssn"),
        @UniqueConstraint(columnNames = "email")
})
@NamedQueries({
        @NamedQuery(name = "FieldEmployee.findAll", query = "SELECT e FROM FieldEmployee e"),
        @NamedQuery(name = "FieldEmployee.findBySsn", query = "SELECT e FROM FieldEmployee e WHERE e.ssn = :ssn")
})
public class FieldEmployee {

    // Dependencies

    // Fields

    @Id
    @GeneratedValue
    @JsonView(NoSsn.class)
    private Long id;
    @Column(nullable = false)
    @JsonView(NoSsn.class)
    private String firstName;
    @Column(nullable = false)
    @JsonView(NoSsn.class)
    private String lastName;
    @Column(nullable = false, unique = true)
    private String ssn;
    @Column(nullable = false, unique = true)
    @JsonView(NoSsn.class)
    private String email;
    @ElementCollection(fetch = FetchType.LAZY)
    @JsonView(NoSsn.class)
    private Map<String, String> properties = new LinkedHashMap<>();
    @ElementCollection(fetch = FetchType.LAZY)
    @JsonView(NoSsn.class)
    private Set<String> tags = new HashSet<>();
    @OneToOne
    @JsonView(NoSsn.class)
    private FieldEmploymentContract employmentContract;
    @ManyToOne
    @JsonView(NoSsn.class)
    private FieldCompany company;
    @ManyToMany
    @JsonView(NoSsn.class)
    private Set<FieldRole> roles = new LinkedHashSet<>();


    // Constructors

    public FieldEmployee() {
    }

    public FieldEmployee(String firstName, String lastName, String ssn, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.ssn = ssn;
        this.email = email;
    }

    public FieldEmployee(String firstName, String lastName, String ssn, String email, FieldCompany company) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.ssn = ssn;
        this.email = email;
        this.company = company;
    }

    public FieldEmployee(String firstName, String lastName, String ssn, String email, FieldCompany company, FieldEmploymentContract employmentContract) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.ssn = ssn;
        this.email = email;
        this.company = company;
        this.employmentContract = employmentContract;
    }

    // Methods

    public FieldEmployee addRole(FieldRole employee) {
        this.getRoles().add(employee);

        return this;
    }

    public boolean hasOperation(FieldOperation operation) {
        return getRoles().stream().map(FieldRole::getOperations).flatMap(Collection::stream).anyMatch(o -> o.equals(operation));
    }

    // Accessors

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public FieldCompany getCompany() {
        return company;
    }

    public void setCompany(FieldCompany company) {
        this.company = company;
    }

    public FieldEmploymentContract getEmploymentContract() {
        return employmentContract;
    }

    public void setEmploymentContract(FieldEmploymentContract employmentContract) {
        this.employmentContract = employmentContract;
    }

    public Set<FieldRole> getRoles() {
        return roles;
    }

    public void setRoles(Set<FieldRole> roles) {
        this.roles = roles;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public List<FieldOperation> getOperations() {
        return this.getRoles().stream()
                .map(FieldRole::getOperations)
                .flatMap(Collection::stream)
                .distinct()
                .sorted(Comparator.comparing(Enum::name))
                .collect(Collectors.toList());
    }

}