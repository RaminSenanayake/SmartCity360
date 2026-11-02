package lk.urbantechsolutions.smartcity360.model.zone;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cities")
public class City implements Zone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "city")
    private List<District> districts = new ArrayList<>();

    public City(String name) {
        this.name = name;
    }

    public City() {

    }

    public void addDistrict(District district) {
        districts.add(district);
        district.setCity(this);
    }

    public void removeDistrict(District district) {
        districts.remove(district);
        district.setCity(null);
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
        return districts.stream().anyMatch(District::isPowerOn);
    }

    @Override
    public void turnOnPower() {
        districts.forEach(District::turnOnPower);
    }

    @Override
    public void shutDownPower() {
        districts.forEach(District::shutDownPower);
    }

    @Override
    public BigDecimal getPowerUsage() {
        return districts.stream().map(District::getPowerUsage).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
