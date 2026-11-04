package lk.urbantechsolutions.smartcity360.model.role;

import jakarta.persistence.*;
import lk.urbantechsolutions.smartcity360.model.resident.Complaint;
import lk.urbantechsolutions.smartcity360.model.resident.ComplaintSeverity;

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
    protected String getRole() {
        return "Supervisor";
    }

    @Override
    protected boolean canHandle(Complaint complaint) {
        return complaint.getSeverity().equals(ComplaintSeverity.MEDIUM);
    }

    @Override
    protected Staff getNextHandler() { return cityManager; }
}
