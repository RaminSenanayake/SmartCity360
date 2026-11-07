package lk.urbantechsolutions.smartcity360.service;

import lk.urbantechsolutions.smartcity360.model.account.PortalAccount;
import lk.urbantechsolutions.smartcity360.model.role.*;
import lk.urbantechsolutions.smartcity360.repository.*;
import lk.urbantechsolutions.smartcity360.security.PortalUser;
import lk.urbantechsolutions.smartcity360.service.impl.StaffAuthenticationServiceImpl;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StaffSessionAuthenticationTests {
    private final LocalStaffRepository locals = mock(LocalStaffRepository.class);
    private final SupervisorRepository supervisors = mock(SupervisorRepository.class);
    private final CityManagerRepository managers = mock(CityManagerRepository.class);
    private final StaffAuthenticationService authentication = new StaffAuthenticationServiceImpl(locals, supervisors, managers);

    @AfterEach
    void clearSession() { SecurityContextHolder.clearContext(); }

    private void signIn(StaffType type, Long id) {
        PortalUser user = new PortalUser(new PortalAccount("test", "hash", "Test", type, id));
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities()));
    }

    @Test
    void staffSessionReadsOnlyItsOwnIdentityAndCannotOverrideAnInvalidApiToken() {
        when(locals.findById(1L)).thenReturn(Optional.of(new LocalStaff()));
        StaffIdentity identity = new StaffIdentity(StaffType.LOCAL_STAFF, 1L);
        signIn(StaffType.LOCAL_STAFF, 1L);
        assertDoesNotThrow(() -> authentication.authenticate(identity, null));
        assertThrows(SecurityException.class, () -> authentication.authenticate(new StaffIdentity(StaffType.LOCAL_STAFF, 2L), null));
        assertThrows(SecurityException.class, () -> authentication.authenticate(new StaffIdentity(StaffType.SUPERVISOR, 1L), null));
        assertThrows(SecurityException.class, () -> authentication.authenticate(identity, "wrong"));
    }

    @Test
    void residentSessionCannotAuthenticateAsStaff() {
        signIn(null, 1L);
        assertThrows(SecurityException.class,
                () -> authentication.authenticate(new StaffIdentity(StaffType.LOCAL_STAFF, 1L), null));
        verifyNoInteractions(locals, supervisors, managers);
    }

    @Test
    void deletedStaffCannotUseAnExistingSession() {
        signIn(StaffType.LOCAL_STAFF, 1L);
        assertThrows(SecurityException.class,
                () -> authentication.authenticate(new StaffIdentity(StaffType.LOCAL_STAFF, 1L), null));
    }
}
