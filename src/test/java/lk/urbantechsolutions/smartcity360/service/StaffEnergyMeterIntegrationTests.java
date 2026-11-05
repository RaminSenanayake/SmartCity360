package lk.urbantechsolutions.smartcity360.service;

import jakarta.persistence.EntityManager;
import lk.urbantechsolutions.smartcity360.model.zone.ZoneType;
import lk.urbantechsolutions.smartcity360.model.role.*;
import lk.urbantechsolutions.smartcity360.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StaffEnergyMeterIntegrationTests {
    @Autowired private PowerControlService meters;
    @Autowired private LocalStaffRepository locals;
    @Autowired private SupervisorRepository supervisors;
    @Autowired private CityManagerRepository managers;
    @Autowired private BuildingRepository buildings;
    @Autowired private EntityManager entityManager;

    @Test
    @Transactional
    void persistedStaffTokensPermitMeterReadsAndRevocationStopsThem() {
        Long buildingId = buildings.findAll().get(0).getId();
        for (StaffType type : StaffType.values()) {
            Staff staff = switch (type) {
                case LOCAL_STAFF -> locals.findAll().get(0);
                case SUPERVISOR -> supervisors.findAll().get(0);
                case CITY_MANAGER -> managers.findAll().get(0);
            };
            StaffIdentity identity = new StaffIdentity(type, staff.getId());
            String token = staff.rotateAccessToken();
            entityManager.flush();
            entityManager.clear();
            byte[] reading = meters.getUsage(buildingId, ZoneType.BUILDING, identity, token, false, false);
            assertTrue(new String(reading, StandardCharsets.UTF_8).contains("\"buildingId\":" + buildingId));
            Staff reloaded = switch (type) {
                case LOCAL_STAFF -> locals.findById(identity.id()).orElseThrow();
                case SUPERVISOR -> supervisors.findById(identity.id()).orElseThrow();
                case CITY_MANAGER -> managers.findById(identity.id()).orElseThrow();
            };
            reloaded.revokeAccessToken();
            entityManager.flush();
            entityManager.clear();
            assertThrows(SecurityException.class,
                    () -> meters.getUsage(buildingId, ZoneType.BUILDING, identity, token, false, false));
        }
    }
}
