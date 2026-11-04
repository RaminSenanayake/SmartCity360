package lk.urbantechsolutions.smartcity360.model.role;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lk.urbantechsolutions.smartcity360.model.resident.Complaint;
import lk.urbantechsolutions.smartcity360.model.resident.ComplaintSeverity;

import java.util.List;

@Entity
@Table(name = "city_managers")
public class CityManager extends Staff {
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "cityManager")
    private List<Supervisor> supervisors;

    @Override
    protected String getRole() {
        return "City Manager";
    }

    @Override
    protected boolean canHandle(Complaint complaint) {
        return complaint.getSeverity().equals(ComplaintSeverity.HIGH);
    }


    @Override
    protected Staff getNextHandler() {
        return null;
    }
}
