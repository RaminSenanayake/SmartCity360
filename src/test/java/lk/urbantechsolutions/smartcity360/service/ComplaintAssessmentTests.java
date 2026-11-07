package lk.urbantechsolutions.smartcity360.service;

import lk.urbantechsolutions.smartcity360.model.resident.*;
import lk.urbantechsolutions.smartcity360.model.role.*;
import lk.urbantechsolutions.smartcity360.repository.*;
import lk.urbantechsolutions.smartcity360.service.impl.ComplaintHandlingServiceImpl;
import org.junit.jupiter.api.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ComplaintAssessmentTests {
    private final ResidentRepository residents = mock(ResidentRepository.class);
    private final ComplaintRepository complaints = mock(ComplaintRepository.class);
    private final LocalStaffRepository locals = mock(LocalStaffRepository.class);
    private final SupervisorRepository supervisors = mock(SupervisorRepository.class);
    private final CityManagerRepository managers = mock(CityManagerRepository.class);
    private final StaffAuthenticationService authentication = mock(StaffAuthenticationService.class);
    private final ComplaintHandlingService service = new ComplaintHandlingServiceImpl(
            residents, complaints, locals, supervisors, managers, authentication);
    private final LocalStaff local = new LocalStaff();
    private final Supervisor supervisor = new Supervisor();
    private final CityManager manager = new CityManager();
    private final StaffIdentity identity = new StaffIdentity(StaffType.LOCAL_STAFF, 1L);
    private Complaint complaint;

    @BeforeEach
    void setup() {
        local.setName("Local reviewer"); supervisor.setName("Supervisor reviewer"); manager.setName("Manager");
        ReflectionTestUtils.setField(local, "id", 1L);
        ReflectionTestUtils.setField(supervisor, "id", 1L);
        ReflectionTestUtils.setField(manager, "id", 1L);
        local.setSupervisor(supervisor); supervisor.setCityManager(manager);
        complaint = new Complaint.Builder(new Resident("Resident"), "Issue").build();
        when(complaints.findById(1L)).thenReturn(Optional.of(complaint));
        when(locals.findById(1L)).thenReturn(Optional.of(local));
        when(supervisors.findById(1L)).thenReturn(Optional.of(supervisor));
        when(managers.findById(1L)).thenReturn(Optional.of(manager));
    }

    @Test
    void submissionIsUnassessedAndDoesNotRequireAvailableStaff() {
        when(residents.findById(1L)).thenReturn(Optional.of(new Resident("Resident")));
        when(complaints.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        Complaint submitted = service.submitComplaint(1L, "  An issue  ");
        assertNull(submitted.getSeverity()); assertFalse(submitted.isHandled());
        assertNull(submitted.getEscalationPath()); assertEquals("An issue", submitted.getComplaint());
        verifyNoInteractions(authentication);
        verify(locals, never()).findAll();
    }

    @Test
    void localReviewerSeveritySelectsTheAppropriateHandlerThroughTheChain() {
        String[] handlers = {"Local Staff", "Supervisor", "City Manager"};
        for (ComplaintSeverity severity : ComplaintSeverity.values()) {
            service.assessComplaint(1L, severity, identity, null);
            assertTrue(complaint.isHandled());
            assertEquals(handlers[severity.ordinal()], complaint.getHandledByRole());
            assertEquals(local.getName(), complaint.getAssessedByName());
            assertNotNull(complaint.getAssessedAt());
        }
    }

    @Test
    void reassessmentClearsTheOldHandlingOutcome() {
        service.assessComplaint(1L, ComplaintSeverity.HIGH, identity, null);
        assertTrue(complaint.getEscalationPath().contains("City Manager"));
        service.assessComplaint(1L, ComplaintSeverity.LOW, identity, null);
        assertEquals("Local Staff", complaint.getHandledByRole());
        assertEquals(local.getName(), complaint.getHandledByName());
        assertFalse(complaint.getEscalationPath().contains("City Manager"));
    }

    @Test
    void supervisorCanDelegateLowSeverityToTheirLocalStaff() {
        when(locals.findBySupervisor_Id(1L)).thenReturn(List.of(local));
        service.assessComplaint(1L, ComplaintSeverity.LOW, new StaffIdentity(StaffType.SUPERVISOR, 1L), null);
        assertEquals(supervisor.getName(), complaint.getAssessedByName());
        assertEquals("Local Staff", complaint.getHandledByRole());
    }

    @Test
    void cityManagerCanSetEverySeverityAndDelegateWithinTheirOwnTeam() {
        when(supervisors.findByCityManager_Id(1L)).thenReturn(List.of(supervisor));
        when(locals.findBySupervisor_Id(1L)).thenReturn(List.of(local));
        String[] handlers = {"Local Staff", "Supervisor", "City Manager"};
        for (ComplaintSeverity severity : ComplaintSeverity.values()) {
            service.assessComplaint(1L, severity, new StaffIdentity(StaffType.CITY_MANAGER, 1L), null);
            assertTrue(complaint.isHandled());
            assertEquals(handlers[severity.ordinal()], complaint.getHandledByRole());
            assertEquals(manager.getName(), complaint.getAssessedByName());
            assertEquals("CITY MANAGER", complaint.getAssessedByRole());
        }
    }

    @Test
    void missingManagerTeamDoesNotChangeTheExistingAssessment() {
        service.assessComplaint(1L, ComplaintSeverity.HIGH, new StaffIdentity(StaffType.CITY_MANAGER, 1L), null);
        for (ComplaintSeverity severity : new ComplaintSeverity[]{ComplaintSeverity.LOW, ComplaintSeverity.MEDIUM}) {
            assertThrows(IllegalStateException.class,
                    () -> service.assessComplaint(1L, severity, new StaffIdentity(StaffType.CITY_MANAGER, 1L), null));
            assertEquals(ComplaintSeverity.HIGH, complaint.getSeverity());
            assertEquals("City Manager", complaint.getHandledByRole());
        }
    }

    @Test
    void unauthorizedOrUnauthenticatedAssessmentDoesNotModifyComplaint() {
        assertThrows(AccessDeniedException.class,
                () -> service.assessComplaint(1L, ComplaintSeverity.HIGH, null, null));
        assertThrows(AccessDeniedException.class,
                () -> service.assessComplaint(1L, ComplaintSeverity.HIGH, new StaffIdentity(null, 1L), null));
        doThrow(new SecurityException("Invalid token")).when(authentication).authenticate(identity, "wrong");
        assertThrows(AccessDeniedException.class,
                () -> service.assessComplaint(1L, ComplaintSeverity.HIGH, identity, "wrong"));
        assertNull(complaint.getSeverity());
        verify(complaints, never()).findById(any());
    }

    @Test
    void unassessedComplaintsCannotEnterTheHandlingChain() {
        assertThrows(IllegalStateException.class, () -> local.handleComplaint(complaint));
        assertNull(complaint.getEscalationPath());
    }
}
