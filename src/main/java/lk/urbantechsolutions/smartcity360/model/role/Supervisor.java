package lk.urbantechsolutions.smartcity360.model.role;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "supervisors")
public class Supervisor extends Staff {

    @ManyToOne
    @JoinColumn(name = "city_manager_id")
    private CityManager cityManager;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "supervisor")
    private List<LocalStaff> localStaff;

    public CityManager getCityManager() { return cityManager; }
    public void setCityManager(CityManager cityManager) { this.cityManager = cityManager; }

    @Override
    protected boolean canHandle(Resident resident) {
        return resident.getComplaintSeverity().equals(ComplaintSeverity.MEDIUM);
    }

    @Override
    protected void process(Resident resident) {
        System.out.println("Medium Severity handling");
    }

    @Override
    protected Staff getNextHandler() { return cityManager; }
}
