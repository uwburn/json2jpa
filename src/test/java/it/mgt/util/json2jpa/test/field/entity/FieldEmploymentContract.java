package it.mgt.util.json2jpa.test.field.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
public class FieldEmploymentContract {

    // Fields

    @Id
    @GeneratedValue
    private Long id;
    private String firstName;
    private String lastName;
    private String ssn;
    @Enumerated(EnumType.STRING)
    private FieldEmploymentContractType employmentContractType;
    private Date signatureDate;
    private Date enrollmentDate;
    private Date endDate;
    private int remuneration;
    @OneToOne(mappedBy = "employmentContract")
    private FieldEmployee employee;
    @ManyToOne
    private FieldCompany company;


    // Constructors

    public FieldEmploymentContract() {
    }

    public FieldEmploymentContract(String firstName, String lastName, String ssn, FieldEmploymentContractType employmentContractType, Date signatureDate, Date enrollmentDate, int remuneration, FieldCompany company) {
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

    public FieldEmployee buildEmployee(String email) {
        return new FieldEmployee(firstName, lastName, ssn, email, company, this);
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

    public FieldEmploymentContractType getEmploymentContractType() {
        return employmentContractType;
    }

    public void setEmploymentContractType(FieldEmploymentContractType employmentContractType) {
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

    public FieldEmployee getEmployee() {
        return employee;
    }

    public void setEmployee(FieldEmployee employee) {
        this.employee = employee;
    }

    public FieldCompany getCompany() {
        return company;
    }

    public void setCompany(FieldCompany company) {
        this.company = company;
    }

}
