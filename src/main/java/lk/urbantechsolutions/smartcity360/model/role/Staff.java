package lk.urbantechsolutions.smartcity360.model.role;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lk.urbantechsolutions.smartcity360.model.resident.Complaint;

import java.util.ArrayList;
import java.util.List;

@MappedSuperclass
public abstract class Staff {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public final Complaint handleComplaint(Complaint complaint) {
        return handle(complaint, new ArrayList<>());
    }

    protected Complaint handle(Complaint complaint, List<String> path) {
        path.add(getRole() + " " + getName());

        if (canHandle(complaint)) {
            complaint = process(complaint);
        } else if (getNextHandler() != null) {
            return getNextHandler().handle(complaint, path);
        }
        // if nobody can handle it, handled stays false

        complaint.setEscalationPath(String.join(" -> ", path));
        return complaint;
    }

    // default behaviour: record who handled it
    protected Complaint process(Complaint complaint) {
        complaint.markHandled(getRole(), getId(), getName());
        return complaint;
    }

    protected abstract String getRole();
    protected abstract boolean canHandle(Complaint complaint);
    protected abstract Staff getNextHandler();
}
