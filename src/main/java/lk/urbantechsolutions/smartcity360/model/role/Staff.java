package lk.urbantechsolutions.smartcity360.model.role;

import jakarta.persistence.*;

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

    public final void handleComplaint(Resident resident) {
        if (canHandle(resident)) {
            process(resident);
        } else if (getNextHandler() != null) {
            getNextHandler().handleComplaint(resident);
        } else {
            System.out.println("No one in the chain can handle this complaint");
        }
    }

    protected abstract boolean canHandle(Resident resident);

    protected abstract void process(Resident resident);

    protected abstract Staff getNextHandler();
}
