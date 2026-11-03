package lk.urbantechsolutions.smartcity360.model.role;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "local_staff")
public class LocalStaff extends Staff {

    @ManyToOne
    @JoinColumn(name = "supervisor_id")
    private Supervisor supervisor;

    public Supervisor getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(Supervisor supervisor) {
        this.supervisor = supervisor;
    }

    @Override
    protected boolean canHandle(Resident resident) {
        return resident.getComplaintSeverity().equals(ComplaintSeverity.LOW);
    }

    @Override
    protected void process(Resident resident) {
        System.out.println("Low severity handling");
    }

    @Override
    protected Staff getNextHandler() {
        return supervisor;
    }
}
