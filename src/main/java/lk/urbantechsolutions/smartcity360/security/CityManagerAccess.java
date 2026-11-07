package lk.urbantechsolutions.smartcity360.security;

import lk.urbantechsolutions.smartcity360.service.StaffAuthenticationService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;

/** Shared service-side check for zone power operations and their management view. */
public final class CityManagerAccess {
    private CityManagerAccess() {}

    public static PortalUser require(StaffAuthenticationService staffAuthentication) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof PortalUser user) || !user.isCityManager()) {
            throw new AccessDeniedException("Zone power control is available to city managers only");
        }
        try {
            staffAuthentication.authenticate(user.getStaffIdentity(), null);
        } catch (SecurityException exception) {
            throw new AccessDeniedException("City manager authentication is required", exception);
        }
        return user;
    }
}
