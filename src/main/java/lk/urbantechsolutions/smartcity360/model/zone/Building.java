package lk.urbantechsolutions.smartcity360.model.zone;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "buildings")
public class Building implements Zone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private boolean powerOn;
    @Column(precision = 12, scale = 2)
    private BigDecimal powerUsage;

    @ManyToOne
    @JoinColumn(name = "street_id")
    private Street street;

    public Building() {

    }

    public Street getStreet() {
        return street;
    }

    public void setStreet(Street street) {
        this.street = street;
    }

    public Building(String name, BigDecimal powerUsage) {
        this.name = name;
        this.powerUsage = powerUsage;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean isPowerOn() {
        return powerOn;
    }

    @Override
    public void turnOnPower() {
        this.powerOn = true;
    }

    @Override
    public void shutDownPower() {
        this.powerOn = false;
    }

    public BigDecimal getPowerUsage() {
        return powerUsage;
    }

    public void setPowerUsage(BigDecimal usage) {
        this.powerUsage = usage;
    }
}
