package lk.urbantechsolutions.smartcity360.service;

import lk.urbantechsolutions.smartcity360.model.zone.District;

import java.util.List;

public interface DistrictService {
    List<District> getByCity(Long cityId);
    District get(Long id);
    District add(Long cityId, String name);
    District update(Long id, String name);
    void remove(Long id);
}
