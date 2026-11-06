package lk.urbantechsolutions.smartcity360.model.policy;

import jakarta.persistence.*;

@Entity
@Table(name = "city_policies")
public class CityPolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(nullable = false, length = 4096)
    private String rule;

    public CityPolicy() {}

    public CityPolicy(String name, String rule) {
        this.name = name;
        this.rule = rule;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRule() { return rule; }
    public void setRule(String rule) { this.rule = rule; }
}
