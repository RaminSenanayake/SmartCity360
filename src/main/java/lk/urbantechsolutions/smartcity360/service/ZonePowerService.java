package lk.urbantechsolutions.smartcity360.service;

import jakarta.persistence.EntityNotFoundException;
import lk.urbantechsolutions.smartcity360.model.zone.*;
import lk.urbantechsolutions.smartcity360.model.streetlight.Streetlight;
import lk.urbantechsolutions.smartcity360.repository.*;
import lk.urbantechsolutions.smartcity360.security.CityManagerAccess;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ZonePowerService {
    public record ZoneRow(Long id, ZoneType type, String name, String location, long on, long total) {
        public String state() { return total == 0 ? "No devices" : on == 0 ? "Off" : on == total ? "On" : "Mixed"; }
    }
    private final CityRepository cities;
    private final DistrictRepository districts;
    private final StreetRepository streets;
    private final BuildingRepository buildings;
    private final StreetlightRepository lights;
    private final StaffAuthenticationService authentication;

    public ZonePowerService(CityRepository cities, DistrictRepository districts, StreetRepository streets,
                            BuildingRepository buildings, StreetlightRepository lights, StaffAuthenticationService authentication) {
        this.cities = cities; this.districts = districts; this.streets = streets;
        this.buildings = buildings; this.lights = lights; this.authentication = authentication;
    }

    public List<ZoneRow> getZones(Long cityId) {
        CityManagerAccess.require(authentication);
        List<City> selection = cityId == null ? cities.findAll() : List.of(cities.findById(cityId)
                .orElseThrow(() -> new EntityNotFoundException("City " + cityId + " not found")));
        List<ZoneRow> rows = new ArrayList<>();
        for (City city : selection) {
            List<ZoneRow> cityRows = new ArrayList<>();
            long cityOn = 0, cityTotal = 0;
            for (District district : districts.findByCity_Id(city.getId())) {
                List<ZoneRow> districtRows = new ArrayList<>();
                long districtOn = 0, districtTotal = 0;
                for (Street street : streets.findByDistrict_Id(district.getId())) {
                    var streetBuildings = buildings.findByStreet_Id(street.getId());
                    var streetLights = lights.getAllByStreet_Id(street.getId());
                    long total = streetBuildings.size() + streetLights.size();
                    long on = streetBuildings.stream().filter(Building::isPowerOn).count()
                            + streetLights.stream().filter(Streetlight::isPowerOn).count();
                    String location = city.getName() + " / " + district.getName();
                    districtRows.add(new ZoneRow(street.getId(), ZoneType.STREET, street.getName(), location, on, total));
                    for (Building building : streetBuildings) {
                        districtRows.add(new ZoneRow(building.getId(), ZoneType.BUILDING, building.getName(),
                                location + " / " + street.getName(), building.isPowerOn() ? 1 : 0, 1));
                    }
                    districtOn += on; districtTotal += total;
                }
                cityRows.add(new ZoneRow(district.getId(), ZoneType.DISTRICT, district.getName(), city.getName(), districtOn, districtTotal));
                cityRows.addAll(districtRows);
                cityOn += districtOn; cityTotal += districtTotal;
            }
            rows.add(new ZoneRow(city.getId(), ZoneType.CITY, city.getName(), "City network", cityOn, cityTotal));
            rows.addAll(cityRows);
        }
        return rows;
    }
}
