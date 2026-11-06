package lk.urbantechsolutions.smartcity360.service.impl;

import lk.urbantechsolutions.smartcity360.model.energymeter.*;
import lk.urbantechsolutions.smartcity360.model.role.StaffIdentity;
import lk.urbantechsolutions.smartcity360.model.zone.ZoneType;
import lk.urbantechsolutions.smartcity360.repository.BuildingRepository;
import lk.urbantechsolutions.smartcity360.repository.CityRepository;
import lk.urbantechsolutions.smartcity360.repository.DistrictRepository;
import lk.urbantechsolutions.smartcity360.repository.StreetRepository;
import lk.urbantechsolutions.smartcity360.service.PowerControlService;
import lk.urbantechsolutions.smartcity360.service.StaffAuthenticationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class PowerControlServiceImpl implements PowerControlService {

    private CityRepository cityRepository;
    private DistrictRepository districtRepository;
    private StreetRepository streetRepository;
    private BuildingRepository buildingRepository;
    private final StaffAuthenticationService authenticationService;
    private final String encryptionKey;

    public PowerControlServiceImpl(
            CityRepository cityRepository,
            DistrictRepository districtRepository,
            StreetRepository streetRepository,
            BuildingRepository buildingRepository,
            StaffAuthenticationService authenticationService,
            @Value("${energy-meter.encryption-key:}") String encryptionKey
    ) {
        this.cityRepository = cityRepository;
        this.districtRepository = districtRepository;
        this.streetRepository = streetRepository;
        this.buildingRepository = buildingRepository;
        this.authenticationService = authenticationService;
        this.encryptionKey = encryptionKey;
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
    @Transactional(readOnly = true)
    public BigDecimal getUsage(Long zoneId, ZoneType zoneType) {
        return switch (zoneType) {
            case CITY -> cityRepository.findById(zoneId).orElseThrow().getPowerUsage();
            case DISTRICT -> districtRepository.findById(zoneId).orElseThrow().getPowerUsage();
            case STREET -> streetRepository.findById(zoneId).orElseThrow().getPowerUsage();
            case BUILDING ->
                    throw new SecurityException("Building usage requires authenticated staff; use the credentialed getUsage overload");
        };
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getUsage(Long zoneId, ZoneType zoneType, StaffIdentity staff, String token,
                           boolean compress, boolean encrypt) {
        return switch (zoneType) {
            case CITY, DISTRICT, STREET -> {
                if (compress || encrypt) {
                    throw new IllegalArgumentException("Stream decorators apply only to building meters");
                }
                yield getUsage(zoneId, zoneType).toPlainString().getBytes(StandardCharsets.UTF_8);
            }
            case BUILDING -> {
                // The outer decorator must authenticate before lookup or encryption-key access.
                EnergyMeterStream stream = () -> {
                    EnergyMeterStream meter = () -> new BuildingEnergyMeter(
                            buildingRepository.findById(zoneId).orElseThrow()).readData();
                    if (compress) {
                        meter = new CompressionDecorator(meter);
                    }
                    if (encrypt) {
                        meter = new EncryptionDecorator(meter, getEncryptionKey());
                    }
                    return meter.readData();
                };
                stream = new AuthenticationDecorator(stream, authenticationService, staff, token);
                yield stream.readData();
            }
        };
    }

    private SecretKey getEncryptionKey() {
        if (encryptionKey == null || encryptionKey.isBlank()) {
            throw new IllegalStateException("Energy meter encryption key is not configured");
        }
        byte[] decoded = Base64.getDecoder().decode(encryptionKey);
        if (decoded.length != 16 && decoded.length != 24 && decoded.length != 32) {
            throw new IllegalArgumentException("Energy meter AES key must contain 16, 24 or 32 bytes");
        }
        return new SecretKeySpec(decoded, "AES");
    }
}
