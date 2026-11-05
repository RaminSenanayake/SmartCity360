package lk.urbantechsolutions.smartcity360.service;

import lk.urbantechsolutions.smartcity360.model.role.*;
import lk.urbantechsolutions.smartcity360.repository.*;
import lk.urbantechsolutions.smartcity360.service.impl.StaffAuthenticationServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StaffAuthenticationServiceTests {
    private final LocalStaffRepository locals = mock(LocalStaffRepository.class);
    private final SupervisorRepository supervisors = mock(SupervisorRepository.class);
    private final CityManagerRepository managers = mock(CityManagerRepository.class);
    private final StaffAuthenticationService authentication =
            new StaffAuthenticationServiceImpl(locals, supervisors, managers);

    @Test
    void allStaffRolesAuthenticateWithTheirOwnCredentials() {
        LocalStaff local = new LocalStaff();
        Supervisor supervisor = new Supervisor();
        CityManager manager = new CityManager();
        when(locals.findById(1L)).thenReturn(Optional.of(local));
        when(supervisors.findById(1L)).thenReturn(Optional.of(supervisor));
        when(managers.findById(1L)).thenReturn(Optional.of(manager));
        assertDoesNotThrow(() -> authentication.authenticate(
                new StaffIdentity(StaffType.LOCAL_STAFF, 1L), local.rotateAccessToken()));
        assertDoesNotThrow(() -> authentication.authenticate(
                new StaffIdentity(StaffType.SUPERVISOR, 1L), supervisor.rotateAccessToken()));
        assertDoesNotThrow(() -> authentication.authenticate(
                new StaffIdentity(StaffType.CITY_MANAGER, 1L), manager.rotateAccessToken()));
    }

    @Test
    void knowingStaffIdOrUsingAnotherStaffTokenDoesNotAuthenticate() {
        LocalStaff first = new LocalStaff();
        String token = first.rotateAccessToken();
        LocalStaff second = new LocalStaff();
        second.rotateAccessToken();
        Supervisor supervisor = new Supervisor();
        supervisor.rotateAccessToken();
        when(locals.findById(1L)).thenReturn(Optional.of(first));
        when(locals.findById(2L)).thenReturn(Optional.of(second));
        when(supervisors.findById(1L)).thenReturn(Optional.of(supervisor));
        assertThrows(SecurityException.class, () -> authentication.authenticate(
                new StaffIdentity(StaffType.LOCAL_STAFF, 1L), "wrong"));
        assertThrows(SecurityException.class, () -> authentication.authenticate(
                new StaffIdentity(StaffType.LOCAL_STAFF, 2L), token));
        assertThrows(SecurityException.class, () -> authentication.authenticate(
                new StaffIdentity(StaffType.SUPERVISOR, 1L), token));
    }

    @Test
    void missingDeletedAndUnprovisionedStaffCannotAuthenticate() {
        LocalStaff local = new LocalStaff();
        when(locals.findById(1L)).thenReturn(Optional.of(local));
        StaffIdentity identity = new StaffIdentity(StaffType.LOCAL_STAFF, 1L);
        assertThrows(SecurityException.class, () -> authentication.authenticate(identity, "anything"));
        String token = local.rotateAccessToken();
        when(locals.findById(1L)).thenReturn(Optional.empty());
        assertThrows(SecurityException.class, () -> authentication.authenticate(identity, token));
    }

    @Test
    void rotationAndRevocationInvalidatePreviouslyIssuedTokens() {
        LocalStaff local = new LocalStaff();
        when(locals.findById(1L)).thenReturn(Optional.of(local));
        StaffIdentity identity = new StaffIdentity(StaffType.LOCAL_STAFF, 1L);
        String oldToken = local.rotateAccessToken();
        String newToken = local.rotateAccessToken();
        assertNotEquals(oldToken, newToken);
        assertThrows(SecurityException.class, () -> authentication.authenticate(identity, oldToken));
        assertDoesNotThrow(() -> authentication.authenticate(identity, newToken));
        local.revokeAccessToken();
        assertThrows(SecurityException.class, () -> authentication.authenticate(identity, newToken));
    }

    @Test
    void malformedIdentityAndMissingCredentialsAreRejectedBeforeLookup() {
        for (StaffIdentity identity : new StaffIdentity[]{null, new StaffIdentity(null, 1L),
                new StaffIdentity(StaffType.LOCAL_STAFF, null), new StaffIdentity(StaffType.LOCAL_STAFF, 0L)}) {
            assertThrows(SecurityException.class, () -> authentication.authenticate(identity, "token"));
        }
        for (String token : new String[]{null, "", "  "}) {
            assertThrows(SecurityException.class, () -> authentication.authenticate(
                    new StaffIdentity(StaffType.LOCAL_STAFF, 1L), token));
        }
        verifyNoInteractions(locals, supervisors, managers);
    }
}
