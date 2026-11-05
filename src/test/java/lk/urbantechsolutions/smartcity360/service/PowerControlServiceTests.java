package lk.urbantechsolutions.smartcity360.service;

import lk.urbantechsolutions.smartcity360.model.zone.*;
import lk.urbantechsolutions.smartcity360.repository.*;
import lk.urbantechsolutions.smartcity360.service.impl.PowerControlServiceImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PowerControlServiceTests {
    private final CityRepository cities = mock(CityRepository.class);
    private final DistrictRepository districts = mock(DistrictRepository.class);
    private final StreetRepository streets = mock(StreetRepository.class);
    private final BuildingRepository buildings = mock(BuildingRepository.class);
    private final StaffAuthenticationService authentication = mock(StaffAuthenticationService.class);
    private final PowerControlService service = new PowerControlServiceImpl(
            cities, districts, streets, buildings, authentication, "");

    @Test
    void numericBuildingOverloadCannotBypassAuthentication() {
        assertThrows(SecurityException.class, () -> service.getUsage(1L, ZoneType.BUILDING));
        verifyNoInteractions(buildings);
    }

    @Test
    void aggregateUsageStillUsesCompositeTotals() {
        City city = mock(City.class);
        District district = mock(District.class);
        Street street = mock(Street.class);
        BigDecimal total = new BigDecimal("123.45");
        when(cities.findById(1L)).thenReturn(Optional.of(city));
        when(districts.findById(1L)).thenReturn(Optional.of(district));
        when(streets.findById(1L)).thenReturn(Optional.of(street));
        when(city.getPowerUsage()).thenReturn(total);
        when(district.getPowerUsage()).thenReturn(total);
        when(street.getPowerUsage()).thenReturn(total);
        for (ZoneType type : new ZoneType[]{ZoneType.CITY, ZoneType.DISTRICT, ZoneType.STREET}) {
            assertEquals(total, service.getUsage(1L, type));
            assertEquals("123.45", new String(service.getUsage(1L, type, null, null, false, false),
                    StandardCharsets.UTF_8));
            assertThrows(IllegalArgumentException.class,
                    () -> service.getUsage(1L, type, null, null, true, false));
            assertThrows(IllegalArgumentException.class,
                    () -> service.getUsage(1L, type, null, null, false, true));
        }
        verifyNoInteractions(buildings, authentication);
    }
}
