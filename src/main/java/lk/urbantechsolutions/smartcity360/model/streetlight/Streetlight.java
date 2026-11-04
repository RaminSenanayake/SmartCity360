package lk.urbantechsolutions.smartcity360.model.streetlight;

import jakarta.persistence.*;
import lk.urbantechsolutions.smartcity360.model.zone.Street;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "streetlights")
public class Streetlight implements AbstractStreetlight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "street_id")
    private Street street;

    @Column(precision = 9, scale = 4)
    private BigDecimal longitude;
    @Column(precision = 9, scale = 4)
    private BigDecimal latitude;

    @Transient
    private boolean powerOn;

    @ManyToOne(optional = false)
    @JoinColumn(name = "type_id")
    private SharedStreetlight sharedStreetlight;

    public Streetlight(BigDecimal longitude, BigDecimal latitude, SharedStreetlight sharedStreetlight) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.sharedStreetlight = sharedStreetlight;
    }

    protected Streetlight() {
    }

    public Long getId() {
        return id;
    }

    public SharedStreetlight getSharedStreetlight() {
        return sharedStreetlight;
    }

    public void setSharedStreetlight(SharedStreetlight s) {
        this.sharedStreetlight = s;
    }

    public Street getStreet() {
        return street;
    }

    public void setStreet(Street street) {
        this.street = street;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public boolean isPowerOn() {
        return powerOn;
    }

    public void turnPowerOn() {
        this.powerOn = true;
    }

    public void turnPowerOff() {
        this.powerOn = false;
    }

    /* Assuming a light is kept on for 12 hours a day (sunset to sunrise)
     * usage is returned as kWh */
    @Override
    public BigDecimal getUsage() {
        return sharedStreetlight.getWattage()
                .multiply(BigDecimal.valueOf(12))
                .divide(BigDecimal.valueOf(1000), RoundingMode.CEILING);
    }
}
