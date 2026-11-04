package lk.urbantechsolutions.smartcity360.model.role;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lk.urbantechsolutions.smartcity360.model.resident.Complaint;
import lk.urbantechsolutions.smartcity360.model.resident.ComplaintSeverity;

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
    protected String getRole() {
        return "Local Staff";
    }

    @Override
    protected boolean canHandle(Complaint complaint) {
        return complaint.getSeverity().equals(ComplaintSeverity.LOW);
    }

    @Override
    protected Staff getNextHandler() {
        return supervisor;
    }
}
