package it.mgt.json2jpa.test.property.entity;

import javax.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("base")
public abstract class PropertyOrganization {

    // Fields

    private Long id;
    private String name;
    private String address;


    // Constructors

    public PropertyOrganization() {
    }

    public PropertyOrganization(String name, String address) {
        this.name = name;
        this.address = address;
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

    @Column(nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(nullable = false)
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

}
