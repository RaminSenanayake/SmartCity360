package lk.urbantechsolutions.smartcity360.security;

import lk.urbantechsolutions.smartcity360.repository.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortalUserDetailsService implements UserDetailsService {
    private final PortalAccountRepository accounts;
    private final ResidentRepository residents;
    private final LocalStaffRepository locals;
    private final SupervisorRepository supervisors;
    private final CityManagerRepository managers;

    public PortalUserDetailsService(PortalAccountRepository accounts, ResidentRepository residents,
                                   LocalStaffRepository locals, SupervisorRepository supervisors,
                                   CityManagerRepository managers) {
        this.accounts = accounts; this.residents = residents; this.locals = locals;
        this.supervisors = supervisors; this.managers = managers;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        var account = accounts.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
        boolean exists = account.isStaff() ? switch (account.getStaffType()) {
            case LOCAL_STAFF -> locals.existsById(account.getSubjectId());
            case SUPERVISOR -> supervisors.existsById(account.getSubjectId());
            case CITY_MANAGER -> managers.existsById(account.getSubjectId());
        } : residents.existsById(account.getSubjectId());
        if (!exists) throw new UsernameNotFoundException("Invalid credentials");
        return new PortalUser(account);
    }
}
