package lk.urbantechsolutions.smartcity360.model.streetlight;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "streetlight_types",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"wattage", "color", "color_temperature", "distribution"}))
public class SharedStreetlight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(precision = 8, scale = 2)
    private BigDecimal wattage;
    private String color;
    @Column(name = "color_temperature")
    private int colorTemperature;
    @Enumerated(EnumType.STRING)
    private LightDistribution distribution;

    protected SharedStreetlight() {}

    public SharedStreetlight(BigDecimal wattage, String color,
                             int colorTemperature, LightDistribution distribution) {
        this.wattage = wattage;
        this.color = color;
        this.colorTemperature = colorTemperature;
        this.distribution = distribution;
    }

    public Long getId() { return id; }
    public BigDecimal getWattage() { return wattage; }
    public String getColor() { return color; }
    public int getColorTemperature() { return colorTemperature; }
    public LightDistribution getDistribution() { return distribution; }
}