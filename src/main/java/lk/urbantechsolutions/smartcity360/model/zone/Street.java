package lk.urbantechsolutions.smartcity360.model.zone;

import jakarta.persistence.*;
import lk.urbantechsolutions.smartcity360.model.streetlight.Streetlight;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "streets")
public class Street implements Zone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "street")
    private List<Building> buildings = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "street")
    private List<Streetlight> streetlights = new ArrayList<>();


    @ManyToOne
    @JoinColumn(name = "district_id")
    private District district;

    public Street(String name) {
        this.name = name;
    }

    public Street() {

    }

    public void setDistrict(District district) {
        this.district = district;
    }

    public void addBuilding(Building building) {
        buildings.add(building);
        building.setStreet(this);
    }

    public void removeBuilding(Building building) {
        buildings.remove(building);
        building.setStreet(null);
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
        return buildings.stream().anyMatch(Building::isPowerOn)
                && streetlights.stream().anyMatch(Streetlight::isPowerOn);
    }

    @Override
    public void turnOnPower() {
        buildings.forEach(Building::turnOnPower);
        streetlights.forEach(Streetlight::turnPowerOn);
    }

    @Override
    public void shutDownPower() {
        buildings.forEach(Building::shutDownPower);
        streetlights.forEach(Streetlight::turnPowerOff);
    }

    @Override
    public BigDecimal getPowerUsage() {
        return buildings.stream().map(Building::getPowerUsage)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(
                        streetlights.stream().map(Streetlight::getUsage)
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                );
    }
}
