package lk.urbantechsolutions.smartcity360.model.resident;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "complaints")
public class Complaint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "resident_id")
    private Resident resident;
    private String complaint;
    @Enumerated(EnumType.STRING)
    private ComplaintSeverity severity;

    private boolean handled;
    private String handledByRole;
    private Long handledByStaffId;
    private String handledByName;

    @Column(length = 500)
    private String escalationPath;
    @CreationTimestamp
    private LocalDateTime createdAt;

    private Complaint(Builder builder) {
        this.resident = builder.resident;
        this.complaint = builder.complaint;
        this.severity = builder.severity;
        this.handled = builder.handled;
        this.handledByRole = builder.handledByRole;
        this.handledByStaffId = builder.handledByStaffId;
        this.handledByName = builder.handledByName;
        this.escalationPath = builder.escalationPath;
    }

    protected Complaint() {

    }

    public static class Builder {
        private Resident resident;
        private String complaint;
        private ComplaintSeverity severity;
        private boolean handled;
        private String handledByRole;
        private Long handledByStaffId;
        private String handledByName;
        private String escalationPath;

        public Builder(Resident resident, String complaint) {
            this.resident = resident;
            this.complaint = complaint;
        }

        public Builder setResident(Resident resident) {
            this.resident = resident;
            return this;
        }

        public Builder setComplaint(String complaint) {
            this.complaint = complaint;
            return this;
        }

        public Builder setSeverity(ComplaintSeverity severity) {
            this.severity = severity;
            return this;
        }

        public Builder setHandled(boolean handled) {
            this.handled = handled;
            return this;
        }

        public Builder setHandledByRole(String handledByRole) {
            this.handledByRole = handledByRole;
            return this;
        }

        public Builder setHandledByStaffId(Long handledByStaffId) {
            this.handledByStaffId = handledByStaffId;
            return this;
        }

        public Builder setHandledByName(String handledByName) {
            this.handledByName = handledByName;
            return this;
        }

        public Builder setEscalationPath(String escalationPath) {
            this.escalationPath = escalationPath;
            return this;
        }

        public Complaint build() {
            if (this.resident == null) {
                throw new IllegalStateException("Resident is not selected");
            }
            if (this.complaint == null || this.complaint.isEmpty()) {
                throw new IllegalStateException("Complaint is empty");
            }
            if (this.severity == null) {
                throw new IllegalStateException("Proper complaint severity is not selected");
            }
            return new Complaint(this);
        }
    }

    public void markHandled(String role, Long staffId, String staffName) {
        this.handled = true;
        this.handledByRole = role;
        this.handledByStaffId = staffId;
        this.handledByName = staffName;
    }

    public void setEscalationPath(String escalationPath) { this.escalationPath = escalationPath; }

    public Long getId() { return id; }
    public Resident getResident() { return resident; }
    public String getComplaint() { return complaint; }
    public ComplaintSeverity getSeverity() { return severity; }
    public boolean isHandled() { return handled; }
    public String getHandledByRole() { return handledByRole; }
    public Long getHandledByStaffId() { return handledByStaffId; }
    public String getHandledByName() { return handledByName; }
    public String getEscalationPath() { return escalationPath; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
