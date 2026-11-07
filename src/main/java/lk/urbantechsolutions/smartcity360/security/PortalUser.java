package lk.urbantechsolutions.smartcity360.security;

import lk.urbantechsolutions.smartcity360.model.account.PortalAccount;
import lk.urbantechsolutions.smartcity360.model.role.StaffIdentity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import java.util.List;

public class PortalUser extends User {
    private final Long accountId;
    private final String displayName;
    private final Long subjectId;
    private final StaffIdentity staffIdentity;

    public PortalUser(PortalAccount account) {
        super(account.getUsername(), account.getPasswordHash(),
                List.of(new SimpleGrantedAuthority(account.isStaff() ? "ROLE_STAFF" : "ROLE_RESIDENT")));
        accountId = account.getId();
        displayName = account.getDisplayName();
        subjectId = account.getSubjectId();
        staffIdentity = account.isStaff() ? new StaffIdentity(account.getStaffType(), subjectId) : null;
    }

    public Long getAccountId() { return accountId; }
    public String getDisplayName() { return displayName; }
    public Long getSubjectId() { return subjectId; }
    public StaffIdentity getStaffIdentity() { return staffIdentity; }
    public boolean isStaff() { return staffIdentity != null; }
    public boolean isCityManager() {
        return isStaff() && staffIdentity.type() == lk.urbantechsolutions.smartcity360.model.role.StaffType.CITY_MANAGER;
    }
    public boolean isComplaintReviewer() {
        return isStaff();
    }
    public String getRoleLabel() {
        return isStaff() ? staffIdentity.type().name().replace('_', ' ').toLowerCase(java.util.Locale.ROOT) : "resident";
    }
}
