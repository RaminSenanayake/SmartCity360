package lk.urbantechsolutions.smartcity360.model.account;

import jakarta.persistence.*;
import lk.urbantechsolutions.smartcity360.model.role.StaffType;

/** Browser credentials are separate from staff meter API tokens. */
@Entity
@Table(name = "portal_accounts")
public class PortalAccount {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String username;
    @Column(nullable = false)
    private String passwordHash;
    @Column(nullable = false)
    private String displayName;
    @Enumerated(EnumType.STRING)
    private StaffType staffType;
    @Column(nullable = false)
    private Long subjectId;

    protected PortalAccount() {}

    public PortalAccount(String username, String passwordHash, String displayName, StaffType staffType, Long subjectId) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.staffType = staffType;
        this.subjectId = subjectId;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getDisplayName() { return displayName; }
    public StaffType getStaffType() { return staffType; }
    public Long getSubjectId() { return subjectId; }
    public boolean isStaff() { return staffType != null; }
}
