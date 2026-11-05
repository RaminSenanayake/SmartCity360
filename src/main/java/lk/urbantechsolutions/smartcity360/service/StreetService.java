package lk.urbantechsolutions.smartcity360.service;

import lk.urbantechsolutions.smartcity360.model.zone.Street;

import java.util.List;

public interface StreetService {
    List<Street> getByDistrict(Long districtId);
    Street get(Long id);
    Street add(Long districtId, String name);
    Street update(Long id, String name);
    void remove(Long id);
}
