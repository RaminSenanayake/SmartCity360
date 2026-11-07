package lk.urbantechsolutions.smartcity360.service;

import lk.urbantechsolutions.smartcity360.model.zone.*;
import lk.urbantechsolutions.smartcity360.model.streetlight.Streetlight;
import lk.urbantechsolutions.smartcity360.repository.*;
import lk.urbantechsolutions.smartcity360.security.PortalUser;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class ZonePowerIntegrationTests {
    @Autowired private PowerControlService power;
    @Autowired private ZonePowerService controls;
    @Autowired private CityRepository cities;
    @Autowired private BuildingRepository buildings;
    @Autowired private StreetlightRepository lights;
    @Autowired private SharedStreetlightRepository shared;
    @Autowired private UserDetailsService users;
    @Autowired private WebApplicationContext context;
    private Long cityId, districtId, streetId, firstId, secondId, lightId;
    private PortalUser manager;
    private MockMvc mvc;

    @BeforeEach
    void setup() {
        manager = (PortalUser) users.loadUserByUsername("city_manager.1");
        signIn(manager);
        City city = new City("Power control test city");
        District district = new District("Power control test district");
        Street street = new Street("Power control test street");
        Building first = new Building("Test building one", new BigDecimal("12.50"));
        Building second = new Building("Test building two", new BigDecimal("25.00"));
        second.turnOnPower();
        city.addDistrict(district); district.addStreet(street);
        street.addBuilding(first); street.addBuilding(second);
        cities.saveAndFlush(city);
        Streetlight light = new Streetlight(new BigDecimal("79.8"), new BigDecimal("7.2"), shared.findAll().get(0));
        light.setStreet(street); lights.saveAndFlush(light);
        cityId = city.getId(); districtId = district.getId(); streetId = street.getId();
        firstId = first.getId(); secondId = second.getId(); lightId = light.getId();
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    private void signIn(PortalUser user) {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities()));
    }

    @AfterEach
    void cleanup() {
        cities.deleteById(cityId);
        SecurityContextHolder.clearContext();
    }

    @Test
    void commandsPersistAndCascadeToBuildingsAndStreetlightsAtEveryCompositeLevel() {
        // No enclosing test transaction: each service call must commit its own writes.
        Long[] ids = {cityId, districtId, streetId};
        ZoneType[] types = {ZoneType.CITY, ZoneType.DISTRICT, ZoneType.STREET};
        for (int i = 0; i < ids.length; i++) {
            power.turnOffPower(ids[i], types[i]);
            assertFalse(buildings.findById(firstId).orElseThrow().isPowerOn());
            assertFalse(buildings.findById(secondId).orElseThrow().isPowerOn());
            assertFalse(lights.findById(lightId).orElseThrow().isPowerOn());
            power.turnOnPower(ids[i], types[i]);
            assertTrue(buildings.findById(firstId).orElseThrow().isPowerOn());
            assertTrue(buildings.findById(secondId).orElseThrow().isPowerOn());
            assertTrue(lights.findById(lightId).orElseThrow().isPowerOn());
        }
        power.turnOffPower(firstId, ZoneType.BUILDING);
        assertFalse(buildings.findById(firstId).orElseThrow().isPowerOn());
        assertTrue(buildings.findById(secondId).orElseThrow().isPowerOn());
        assertTrue(lights.findById(lightId).orElseThrow().isPowerOn());
        power.turnOnPower(firstId, ZoneType.BUILDING);
        assertTrue(buildings.findById(firstId).orElseThrow().isPowerOn());
    }

    @Test
    void statusDistinguishesMixedOnAndOffAndCountsBothDeviceTypes() {
        var mixed = controls.getZones(cityId).get(0);
        assertEquals("Mixed", mixed.state()); assertEquals(1, mixed.on()); assertEquals(3, mixed.total());
        power.turnOnPower(cityId, ZoneType.CITY);
        assertEquals("On", controls.getZones(cityId).get(0).state());
        power.turnOffPower(cityId, ZoneType.CITY);
        assertEquals("Off", controls.getZones(cityId).get(0).state());
    }

    @Test
    void serviceRejectsNonManagersAndUnauthenticatedCallsBeforeChangingPower() {
        for (String username : new String[]{"resident.1", "local_staff.1", "supervisor.1"}) {
            signIn((PortalUser) users.loadUserByUsername(username));
            assertThrows(AccessDeniedException.class, () -> power.turnOnPower(firstId, ZoneType.BUILDING));
            assertThrows(AccessDeniedException.class, () -> power.turnOffPower(cityId, ZoneType.CITY));
            assertThrows(AccessDeniedException.class, () -> controls.getZones(cityId));
        }
        SecurityContextHolder.clearContext();
        assertThrows(AccessDeniedException.class, () -> power.turnOnPower(firstId, ZoneType.BUILDING));
        assertFalse(buildings.findById(firstId).orElseThrow().isPowerOn());
        assertTrue(buildings.findById(secondId).orElseThrow().isPowerOn());
    }

    @Test
    void httpControlsRequireCityManagerAndCsrfAndRenderForManagers() throws Exception {
        SecurityContextHolder.clearContext();
        for (String username : new String[]{"resident.1", "local_staff.1", "supervisor.1"}) {
            PortalUser user = (PortalUser) users.loadUserByUsername(username);
            mvc.perform(get("/staff/power").with(user(user))).andExpect(status().isForbidden());
            mvc.perform(post("/staff/power").with(user(user)).with(csrf()).param("zoneId", firstId.toString())
                    .param("zoneType", "BUILDING").param("powerOn", "true")).andExpect(status().isForbidden());
            mvc.perform(get("/dashboard").with(user(user))).andExpect(status().isOk())
                    .andExpect(content().string(not(containsString("href=\"/staff/power\""))));
        }
        mvc.perform(get("/staff/power").with(user(manager)).param("cityId", cityId.toString()))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Power control test city")));
        mvc.perform(post("/staff/power").with(user(manager)).param("zoneId", firstId.toString())
                .param("zoneType", "BUILDING").param("powerOn", "true")).andExpect(status().isForbidden());
        mvc.perform(post("/staff/power").with(user(manager)).with(csrf()).param("zoneId", firstId.toString())
                .param("zoneType", "BUILDING").param("powerOn", "true").param("cityId", cityId.toString()))
                .andExpect(redirectedUrl("/staff/power?cityId=" + cityId));
        assertTrue(buildings.findById(firstId).orElseThrow().isPowerOn());
        mvc.perform(post("/staff/power").with(user(manager)).with(csrf()).param("zoneId", Long.toString(Long.MAX_VALUE))
                .param("zoneType", "CITY").param("powerOn", "false")).andExpect(status().isNotFound());
    }
}
