package lk.urbantechsolutions.smartcity360.model.zone;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "districts")
public class District implements Zone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "district")
    private List<Street> streets = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "city_id")
    private City city;

    public District(String name) {
        this.name = name;
    }

    public District() {

    }

    public void setCity(City city) {
        this.city = city;
    }

    public void addStreet(Street street) {
        streets.add(street);
        street.setDistrict(this);
    }

    public void removeStreet(Street street) {
        streets.remove(street);
        street.setDistrict(null);
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
        return streets.stream().anyMatch(Street::isPowerOn);
    }

    @Override
    public void turnOnPower() {
        streets.forEach(Street::turnOnPower);
    }

    @Override
    public void shutDownPower() {
        streets.forEach(Street::shutDownPower);
    }

    @Override
    public BigDecimal getPowerUsage() {
        return streets.stream().map(Street::getPowerUsage).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
