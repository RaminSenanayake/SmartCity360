package lk.urbantechsolutions.smartcity360.service;

import lk.urbantechsolutions.smartcity360.model.zone.City;

import java.util.List;

public interface CityService {
    List<City> getAll();
    City get(Long id);
    City add(String name);
    City update(Long id, String name);
    void remove(Long id);
}
