package lk.urbantechsolutions.smartcity360.service;

import lk.urbantechsolutions.smartcity360.model.zone.Building;

import java.math.BigDecimal;
import java.util.List;

public interface BuildingService {
    List<Building> getByStreet(Long streetId);
    Building get(Long id);
    Building add(Long streetId, String name, BigDecimal powerUsage);
    Building update(Long id, String name, BigDecimal powerUsage);
    void remove(Long id);
}
