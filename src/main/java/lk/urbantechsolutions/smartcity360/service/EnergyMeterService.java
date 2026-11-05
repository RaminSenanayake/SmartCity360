package lk.urbantechsolutions.smartcity360.service;

public interface EnergyMeterService {
    byte[] readData(Long buildingId, String accessToken, boolean compress, boolean encrypt);
}
