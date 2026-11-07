package lk.urbantechsolutions.smartcity360.service.impl;

import lk.urbantechsolutions.smartcity360.model.role.Staff;
import lk.urbantechsolutions.smartcity360.model.role.StaffIdentity;
import lk.urbantechsolutions.smartcity360.repository.CityManagerRepository;
import lk.urbantechsolutions.smartcity360.repository.LocalStaffRepository;
import lk.urbantechsolutions.smartcity360.repository.SupervisorRepository;
import lk.urbantechsolutions.smartcity360.service.StaffAuthenticationService;
import org.springframework.stereotype.Service;
import lk.urbantechsolutions.smartcity360.security.PortalUser;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StaffAuthenticationServiceImpl implements StaffAuthenticationService {
    private final LocalStaffRepository localStaffRepository;
    private final SupervisorRepository supervisorRepository;
    private final CityManagerRepository cityManagerRepository;

    public StaffAuthenticationServiceImpl(LocalStaffRepository localStaffRepository,
                                          SupervisorRepository supervisorRepository,
                                          CityManagerRepository cityManagerRepository) {
        this.localStaffRepository = localStaffRepository;
        this.supervisorRepository = supervisorRepository;
        this.cityManagerRepository = cityManagerRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public void authenticate(StaffIdentity identity, String token) {
        if (identity == null || identity.type() == null || identity.id() == null
                || identity.id() <= 0) {
            throw new SecurityException("Invalid staff credentials");
        }
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean staffSession = token == null && authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof PortalUser user && user.isStaff()
                && identity.equals(user.getStaffIdentity());
        if (!staffSession && (token == null || token.isBlank())) {
            throw new SecurityException("Invalid staff credentials");
        }
        Staff staff = switch (identity.type()) {
            case LOCAL_STAFF -> localStaffRepository.findById(identity.id()).orElse(null);
            case SUPERVISOR -> supervisorRepository.findById(identity.id()).orElse(null);
            case CITY_MANAGER -> cityManagerRepository.findById(identity.id()).orElse(null);
        };
        if (staff == null || (!staffSession && !staff.hasValidAccessToken(token))) {
            throw new SecurityException("Invalid staff credentials");
        }
    }
}
