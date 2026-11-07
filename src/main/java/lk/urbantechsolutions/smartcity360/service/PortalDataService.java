package lk.urbantechsolutions.smartcity360.service;

import lk.urbantechsolutions.smartcity360.model.zone.*;
import lk.urbantechsolutions.smartcity360.repository.*;
import lk.urbantechsolutions.smartcity360.security.PortalUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.json.JsonMapper;
import java.math.BigDecimal;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class PortalDataService {
    public record Choice(Long id, String name) {}
    public record EnergyPoint(Long id, String name, BigDecimal usage) {}
    public record BuildingChoice(Long id, String name, String street, boolean powerOn) {}
    public record Summary(BigDecimal usage, long cities, long districts, long buildings, long poweredBuildings) {}

    private final CityRepository cities;
    private final DistrictRepository districts;
    private final StreetRepository streets;
    private final BuildingRepository buildings;
    private final PowerControlService power;
    private final JsonMapper json = new JsonMapper();

    public PortalDataService(CityRepository cities, DistrictRepository districts, StreetRepository streets,
                             BuildingRepository buildings, PowerControlService power) {
        this.cities = cities; this.districts = districts; this.streets = streets;
        this.buildings = buildings; this.power = power;
    }

    public List<Choice> cityChoices() {
        return cities.findAll().stream().map(city -> new Choice(city.getId(), city.getName())).toList();
    }

    private List<City> selectedCities(Long cityId) {
        if (cityId == null) return cities.findAll();
        return List.of(cities.findById(cityId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "City not found")));
    }

    private List<District> selectedDistricts(Long cityId) {
        if (cityId == null) return districts.findAll();
        selectedCities(cityId);
        return districts.findByCity_Id(cityId);
    }

    private List<Street> selectedStreets(Long cityId) {
        return selectedDistricts(cityId).stream().flatMap(district -> streets.findByDistrict_Id(district.getId()).stream()).toList();
    }

    private List<Building> selectedBuildings(Long cityId) {
        return selectedStreets(cityId).stream().flatMap(street -> buildings.findByStreet_Id(street.getId()).stream()).toList();
    }

    public Summary summary(Long cityId) {
        List<City> selection = selectedCities(cityId);
        List<Building> scopedBuildings = selectedBuildings(cityId);
        BigDecimal usage = selection.stream().map(city -> power.getUsage(city.getId(), ZoneType.CITY))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Summary(usage, selection.size(), selectedDistricts(cityId).size(), scopedBuildings.size(),
                scopedBuildings.stream().filter(Building::isPowerOn).count());
    }

    public List<BuildingChoice> buildingChoices(Long cityId, PortalUser user) {
        if (!user.isStaff()) throw new AccessDeniedException("Staff access required");
        return selectedBuildings(cityId).stream().map(building -> new BuildingChoice(building.getId(), building.getName(),
                building.getStreet().getName(), building.isPowerOn())).toList();
    }

    public List<EnergyPoint> chart(Long cityId, ZoneType level, PortalUser user) {
        List<EnergyPoint> result = switch (level) {
            case CITY -> selectedCities(cityId).stream().map(city -> point(city, level)).toList();
            case DISTRICT -> selectedDistricts(cityId).stream().map(district -> point(district, level)).toList();
            case STREET -> selectedStreets(cityId).stream().map(street -> point(street, level)).toList();
            case BUILDING -> {
                if (!user.isStaff()) throw new AccessDeniedException("Staff access required");
                yield selectedBuildings(cityId).stream().map(building -> {
                    byte[] reading = power.getUsage(building.getId(), ZoneType.BUILDING,
                            user.getStaffIdentity(), null, false, false);
                    return new EnergyPoint(building.getId(), building.getName(),
                            json.readTree(reading).get("powerUsage").decimalValue());
                }).toList();
            }
        };
        return result.stream().sorted(Comparator.comparing(EnergyPoint::usage).reversed()).toList();
    }

    private EnergyPoint point(Zone zone, ZoneType type) {
        return new EnergyPoint(zone.getId(), zone.getName(), power.getUsage(zone.getId(), type));
    }
}
