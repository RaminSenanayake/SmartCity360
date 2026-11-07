package lk.urbantechsolutions.smartcity360.controller;

import lk.urbantechsolutions.smartcity360.security.PortalUser;
import lk.urbantechsolutions.smartcity360.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import java.nio.file.*;
import java.util.Properties;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class PortalWebTests {
    @Autowired private WebApplicationContext context;
    @Autowired private UserDetailsService users;
    @Autowired private BuildingRepository buildings;
    @Autowired private ComplaintRepository complaints;
    @Autowired private CityPolicyRepository policies;
    @org.springframework.beans.factory.annotation.Value("${seed.portal-account-file:.seed/portal-accounts.properties}")
    private String credentialFile;
    private MockMvc mvc;
    private PortalUser resident;
    private PortalUser staff;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        resident = (PortalUser) users.loadUserByUsername("resident.1");
        staff = (PortalUser) users.loadUserByUsername("local_staff.1");
    }

    @Test
    void signInAndAssetsArePublicButWorkspaceRequiresAuthentication() throws Exception {
        mvc.perform(get("/sign-in")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Welcome back")))
                .andExpect(content().string(containsString("_csrf")));
        mvc.perform(get("/css/portal.css")).andExpect(status().isOk());
        mvc.perform(get("/dashboard")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/api/usage")).andExpect(status().is3xxRedirection());
    }

    @Test
    void seededPasswordSignsInAndWrongPasswordIsRejected() throws Exception {
        Properties credentials = new Properties();
        try (var input = Files.newInputStream(Path.of(credentialFile))) { credentials.load(input); }
        mvc.perform(post("/sign-in").with(csrf()).param("portal", "resident").param("username", resident.getUsername())
                        .param("password", credentials.getProperty(resident.getUsername())))
                .andExpect(authenticated().withUsername(resident.getUsername()))
                .andExpect(redirectedUrl("/dashboard"));
        mvc.perform(post("/sign-in").with(csrf()).param("portal", "resident").param("username", resident.getUsername()).param("password", "wrong"))
                .andExpect(unauthenticated()).andExpect(redirectedUrl("/sign-in?error"));
    }

    @Test
    void correctPasswordsCannotSignInThroughTheWrongPortal() throws Exception {
        Properties credentials = new Properties();
        try (var input = Files.newInputStream(Path.of(credentialFile))) { credentials.load(input); }
        for (String username : new String[]{"resident.1", "local_staff.1", "supervisor.1", "city_manager.1"}) {
            boolean isResident = username.startsWith("resident.");
            String correctPortal = isResident ? "resident" : "staff";
            String wrongPortal = isResident ? "staff" : "resident";
            mvc.perform(post("/sign-in").with(csrf()).param("username", username)
                            .param("password", credentials.getProperty(username)).param("portal", wrongPortal))
                    .andExpect(unauthenticated())
                    .andExpect(redirectedUrl(isResident ? "/sign-in?error&portal=staff" : "/sign-in?error"));
            mvc.perform(post("/sign-in").with(csrf()).param("username", username)
                            .param("password", credentials.getProperty(username)).param("portal", correctPortal))
                    .andExpect(authenticated().withUsername(username)).andExpect(redirectedUrl("/dashboard"));
        }
    }

    @Test
    void missingInvalidOrDuplicatePortalSelectionsAreRejected() throws Exception {
        Properties credentials = new Properties();
        try (var input = Files.newInputStream(Path.of(credentialFile))) { credentials.load(input); }
        for (String[] portals : new String[][]{null, {""}, {"admin"}, {"resident", "staff"}, {"resident", "resident"}}) {
            var request = post("/sign-in").with(csrf()).param("username", "resident.1")
                    .param("password", credentials.getProperty("resident.1"));
            if (portals != null) request.param("portal", portals);
            mvc.perform(request).andExpect(unauthenticated()).andExpect(redirectedUrl("/sign-in?error"));
        }
        mvc.perform(get("/sign-in?error&portal=staff")).andExpect(status().isOk())
                .andExpect(model().attribute("selectedPortal", "staff"));
    }

    @Test
    void bothDashboardsAndTemplatesRenderWithDifferentCapabilities() throws Exception {
        mvc.perform(get("/dashboard").with(user(resident))).andExpect(status().isOk())
                .andExpect(content().string(containsString("Resident portal")))
                .andExpect(content().string(not(containsString("data-meter-panel"))))
                .andExpect(content().string(not(containsString("value=\"BUILDING\""))));
        mvc.perform(get("/dashboard").with(user(staff))).andExpect(status().isOk())
                .andExpect(content().string(containsString("data-meter-panel")));
        for (String route : new String[]{"/complaints", "/policies"}) {
            mvc.perform(get(route).with(user(resident))).andExpect(status().isOk());
            mvc.perform(get(route).with(user(staff))).andExpect(status().isOk());
        }
    }

    @Test
    void chartsReturnActualReadingsAndResidentsCannotReadBuildingData() throws Exception {
        mvc.perform(get("/api/usage").with(user(resident))).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5))).andExpect(jsonPath("$[0].usage", greaterThan(0.0)));
        mvc.perform(get("/api/usage").param("level", "BUILDING").with(user(resident))).andExpect(status().isForbidden());
        Long id = buildings.findAll().get(0).getId();
        mvc.perform(get("/staff/meters/" + id).with(user(resident))).andExpect(status().isForbidden());
        mvc.perform(get("/staff/meters/" + id).with(user(staff))).andExpect(status().isOk())
                .andExpect(jsonPath("$.buildingId").value(id)).andExpect(jsonPath("$.powerUsage").exists());
        mvc.perform(get("/api/usage").param("level", "BUILDING").with(user(staff)))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].usage").exists());
    }

    @Test
    void complaintsAreScopedToResidentAndSubmissionUsesSessionIdentity() throws Exception {
        var result = mvc.perform(get("/complaints").with(user(resident))).andReturn();
        @SuppressWarnings("unchecked") var scoped = (java.util.List<lk.urbantechsolutions.smartcity360.model.resident.Complaint>)
                result.getModelAndView().getModel().get("complaints");
        assertTrue(scoped.stream().allMatch(item -> item.getResident().getId().equals(resident.getSubjectId())));
        mvc.perform(post("/resident/complaints").with(user(staff)).with(csrf())
                .param("text", "Do not accept staff submissions").param("severity", "LOW")).andExpect(status().isForbidden());
        mvc.perform(post("/resident/complaints").with(user(resident)).with(csrf())
                .param("text", "Frontend test complaint").param("severity", "LOW").param("residentId", "2"))
                .andExpect(redirectedUrl("/complaints"));
        var submitted = complaints.findByResident_IdOrderByCreatedAtDesc(resident.getSubjectId()).stream()
                .filter(item -> item.getComplaint().equals("Frontend test complaint")).findFirst().orElseThrow();
        assertEquals(resident.getSubjectId(), submitted.getResident().getId());
        assertNull(submitted.getSeverity(), "Resident-supplied severity must be ignored");
        assertFalse(submitted.isHandled());
        complaints.deleteById(submitted.getId());
    }

    @Test
    void policyWritesRequireStaffAndCsrfAndCanBePreviewed() throws Exception {
        mvc.perform(post("/staff/policies").with(user(resident)).with(csrf())
                .param("name", "Blocked").param("rule", "NOISE <= 55")).andExpect(status().isForbidden());
        mvc.perform(post("/staff/policies").with(user(staff))
                .param("name", "Blocked").param("rule", "NOISE <= 55")).andExpect(status().isForbidden());
        mvc.perform(post("/staff/policies/preview").with(user(staff)).with(csrf())
                .param("rule", "NOISE <= 55").param("noise", "40").param("time", "12:00"))
                .andExpect(redirectedUrl("/policies")).andExpect(flash().attribute("previewPass", true));
        long count = policies.count();
        mvc.perform(post("/staff/policies").with(user(staff)).with(csrf())
                .param("name", "Bad rule").param("rule", "not a rule"))
                .andExpect(redirectedUrl("/policies")).andExpect(flash().attributeExists("error"));
        assertEquals(count, policies.count());
    }

    @Test
    void allStaffRolesCanAssessComplaintSeverityButResidentsCannot() throws Exception {
        var residentPage = mvc.perform(get("/complaints").with(user(resident)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertFalse(residentPage.contains("name=\"severity\""));
        var manager = (PortalUser) users.loadUserByUsername("city_manager.1");
        var supervisor = (PortalUser) users.loadUserByUsername("supervisor.1");
        mvc.perform(post("/resident/complaints").with(user(resident)).with(csrf())
                .param("text", "Complaint requiring staff triage")).andExpect(redirectedUrl("/complaints"));
        var complaint = complaints.findByResident_IdOrderByCreatedAtDesc(resident.getSubjectId()).stream()
                .filter(item -> item.getComplaint().equals("Complaint requiring staff triage")).findFirst().orElseThrow();
        String route = "/staff/complaints/" + complaint.getId() + "/severity";
        mvc.perform(post(route).with(user(resident)).with(csrf()).param("severity", "HIGH"))
                .andExpect(status().isForbidden());
        mvc.perform(post(route).with(user(manager)).with(csrf()).param("severity", "HIGH"))
                .andExpect(redirectedUrl("/complaints"));
        var managerAssessment = complaints.findById(complaint.getId()).orElseThrow();
        assertEquals(manager.getDisplayName(), managerAssessment.getAssessedByName());
        assertEquals("City Manager", managerAssessment.getHandledByRole());
        mvc.perform(post("/access-denied").with(user(manager)).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(post(route).with(user(staff)).param("severity", "HIGH")).andExpect(status().isForbidden());
        mvc.perform(post(route).with(user(staff)).with(csrf()).param("severity", "HIGH"))
                .andExpect(redirectedUrl("/complaints"));
        var high = complaints.findById(complaint.getId()).orElseThrow();
        assertEquals(lk.urbantechsolutions.smartcity360.model.resident.ComplaintSeverity.HIGH, high.getSeverity());
        assertEquals("City Manager", high.getHandledByRole());
        assertEquals(staff.getDisplayName(), high.getAssessedByName());
        mvc.perform(post(route).with(user(supervisor)).with(csrf()).param("severity", "LOW"))
                .andExpect(redirectedUrl("/complaints"));
        var low = complaints.findById(complaint.getId()).orElseThrow();
        assertEquals(lk.urbantechsolutions.smartcity360.model.resident.ComplaintSeverity.LOW, low.getSeverity());
        assertEquals("Local Staff", low.getHandledByRole());
        assertEquals(supervisor.getDisplayName(), low.getAssessedByName());
        mvc.perform(get("/complaints").with(user(manager))).andExpect(status().isOk())
                .andExpect(content().string(containsString("class=\"severity-assessment\"")));
        complaints.deleteById(complaint.getId());
    }

    @Test
    void csrfProtectedLogoutEndsTheSession() throws Exception {
        mvc.perform(post("/sign-out").with(user(staff)).with(csrf())).andExpect(unauthenticated())
                .andExpect(redirectedUrl("/sign-in?logout"));
    }
}
