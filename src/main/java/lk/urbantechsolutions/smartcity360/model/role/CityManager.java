package lk.urbantechsolutions.smartcity360.model.role;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.List;

@Entity
@Table(name = "city_managers")
public class CityManager extends Staff {

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "cityManager")
    private List<Supervisor> supervisors;

    @Override
    protected boolean canHandle(Resident resident) {
        return resident.getComplaintSeverity().equals(ComplaintSeverity.HIGH);
    }

    @Override
    protected void process(Resident resident) {
        System.out.println("High Severity handling");
    }

    @Override
    protected Staff getNextHandler() {
        return null;
    }
}
