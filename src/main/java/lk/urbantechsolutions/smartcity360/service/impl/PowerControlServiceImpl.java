package lk.urbantechsolutions.smartcity360.service.impl;

import lk.urbantechsolutions.smartcity360.model.zone.ZoneType;
import lk.urbantechsolutions.smartcity360.repository.BuildingRepository;
import lk.urbantechsolutions.smartcity360.repository.CityRepository;
import lk.urbantechsolutions.smartcity360.repository.DistrictRepository;
import lk.urbantechsolutions.smartcity360.repository.StreetRepository;
import lk.urbantechsolutions.smartcity360.service.PowerControlService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PowerControlServiceImpl implements PowerControlService {

    private CityRepository cityRepository;
    private DistrictRepository districtRepository;
    private StreetRepository streetRepository;
    private BuildingRepository buildingRepository;

    public PowerControlServiceImpl(
            CityRepository cityRepository,
            DistrictRepository districtRepository,
            StreetRepository streetRepository,
            BuildingRepository buildingRepository
    ) {
        this.cityRepository = cityRepository;
        this.districtRepository = districtRepository;
        this.streetRepository = streetRepository;
        this.buildingRepository = buildingRepository;
    }

    @Override
    public void turnOnPower(Long zoneId, ZoneType zoneType) {
        switch (zoneType) {
            case CITY:
                cityRepository.findById(zoneId).orElseThrow().turnOnPower();
                break;
            case DISTRICT:
                districtRepository.findById(zoneId).orElseThrow().turnOnPower();
                break;
            case STREET:
                streetRepository.findById(zoneId).orElseThrow().turnOnPower();
                break;
            case BUILDING:
                buildingRepository.findById(zoneId).orElseThrow().turnOnPower();
                break;
        }
    }

    @Override
    public void turnOffPower(Long zoneId, ZoneType zoneType) {
        switch (zoneType) {
            case CITY:
                cityRepository.findById(zoneId).orElseThrow().shutDownPower();
                break;
            case DISTRICT:
                districtRepository.findById(zoneId).orElseThrow().shutDownPower();
                break;
            case STREET:
                streetRepository.findById(zoneId).orElseThrow().shutDownPower();
                break;
            case BUILDING:
                buildingRepository.findById(zoneId).orElseThrow().shutDownPower();
                break;
        }
    }

    @Override
    public BigDecimal getUsage(Long zoneId, ZoneType zoneType) {
        return switch (zoneType) {
            case CITY -> cityRepository.findById(zoneId).orElseThrow().getPowerUsage();
            case DISTRICT -> districtRepository.findById(zoneId).orElseThrow().getPowerUsage();
            case STREET -> streetRepository.findById(zoneId).orElseThrow().getPowerUsage();
            case BUILDING -> buildingRepository.findById(zoneId).orElseThrow().getPowerUsage();
        };
    }
}
