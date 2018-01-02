package it.mgt.util.json2jpa.test.property.entity;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = "ssn"),
        @UniqueConstraint(columnNames = "email")
})
@NamedQueries({
        @NamedQuery(name = "PropertyEmployee.findAll", query = "SELECT e FROM PropertyEmployee e"),
        @NamedQuery(name = "PropertyEmployee.findBySsn", query = "SELECT e FROM PropertyEmployee e WHERE e.ssn = :ssn")
})
public class PropertyEmployee {

    // Dependencies

    // Fields


    private Long id;
    private String firstName;
    private String lastName;
    private String ssn;
    private String email;
    private Map<String, String> properties = new LinkedHashMap<>();
    private Set<String> tags = new HashSet<>();
    private PropertyEmploymentContract employmentContract;
    private PropertyCompany company;
    private Set<PropertyRole> roles = new LinkedHashSet<>();


    // Constructors

    public PropertyEmployee() {
    }

    public PropertyEmployee(String firstName, String lastName, String ssn, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.ssn = ssn;
        this.email = email;
    }

    public PropertyEmployee(String firstName, String lastName, String ssn, String email, PropertyCompany company) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.ssn = ssn;
        this.email = email;
        this.company = company;
    }

    public PropertyEmployee(String firstName, String lastName, String ssn, String email, PropertyCompany company, PropertyEmploymentContract employmentContract) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.ssn = ssn;
        this.email = email;
        this.company = company;
        this.employmentContract = employmentContract;
    }

    // Methods

    public PropertyEmployee addRole(PropertyRole employee) {
        this.getRoles().add(employee);

        return this;
    }

    public boolean hasOperation(PropertyOperation operation) {
        return getRoles().stream().map(PropertyRole::getOperations).flatMap(Collection::stream).anyMatch(o -> o.equals(operation));
    }

    // Accessors

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(nullable = false)
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Column(nullable = false)
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Column(nullable = false, unique = true)
    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    @Column(nullable = false, unique = true)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @ManyToOne
    public PropertyCompany getCompany() {
        return company;
    }

    public void setCompany(PropertyCompany company) {
        this.company = company;
    }

    @OneToOne
    public PropertyEmploymentContract getEmploymentContract() {
        return employmentContract;
    }

    public void setEmploymentContract(PropertyEmploymentContract employmentContract) {
        this.employmentContract = employmentContract;
    }

    @ManyToMany
    public Set<PropertyRole> getRoles() {
        return roles;
    }

    public void setRoles(Set<PropertyRole> roles) {
        this.roles = roles;
    }

    @ElementCollection(fetch = FetchType.LAZY)
    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @ElementCollection(fetch = FetchType.LAZY)
    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    @Transient
    public List<PropertyOperation> getOperations() {
        return this.getRoles().stream()
                .map(PropertyRole::getOperations)
                .flatMap(Collection::stream)
                .distinct()
                .sorted(Comparator.comparing(Enum::name))
                .collect(Collectors.toList());
    }

}