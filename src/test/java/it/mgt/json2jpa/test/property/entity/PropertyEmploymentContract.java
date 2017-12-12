package it.mgt.json2jpa.test.property.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
public class PropertyEmploymentContract {

    // Fields

    private Long id;
    private String firstName;
    private String lastName;
    private String ssn;
    private PropertyEmploymentContractType employmentContractType;
    private Date signatureDate;
    private Date enrollmentDate;
    private Date endDate;
    private int remuneration;
    private PropertyEmployee employee;
    private PropertyCompany company;


    // Constructors

    public PropertyEmploymentContract() {
    }

    public PropertyEmploymentContract(String firstName, String lastName, String ssn, PropertyEmploymentContractType employmentContractType, Date signatureDate, Date enrollmentDate, int remuneration, PropertyCompany company) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.ssn = ssn;
        this.employmentContractType = employmentContractType;
        this.signatureDate = signatureDate;
        this.enrollmentDate = enrollmentDate;
        this.remuneration = remuneration;
        this.company = company;
    }

    // Methods

    public PropertyEmployee buildEmployee(String email) {
        return new PropertyEmployee(firstName, lastName, ssn, email, company, this);
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

    @Enumerated(EnumType.STRING)
    public PropertyEmploymentContractType getEmploymentContractType() {
        return employmentContractType;
    }

    public void setEmploymentContractType(PropertyEmploymentContractType employmentContractType) {
        this.employmentContractType = employmentContractType;
    }

    public Date getSignatureDate() {
        return signatureDate;
    }

    public void setSignatureDate(Date signatureDate) {
        this.signatureDate = signatureDate;
    }

    public Date getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(Date enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public int getRemuneration() {
        return remuneration;
    }

    public void setRemuneration(int remuneration) {
        this.remuneration = remuneration;
    }

    @OneToOne(mappedBy = "employmentContract")
    public PropertyEmployee getEmployee() {
        return employee;
    }

    public void setEmployee(PropertyEmployee employee) {
        this.employee = employee;
    }

    @ManyToOne
    public PropertyCompany getCompany() {
        return company;
    }

    public void setCompany(PropertyCompany company) {
        this.company = company;
    }

}
