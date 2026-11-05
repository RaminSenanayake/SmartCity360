package lk.urbantechsolutions.smartcity360.service;

import lk.urbantechsolutions.smartcity360.model.role.CityManager;

import java.util.List;

public interface CityManagerService {
    List<CityManager> getAll();
    CityManager get(Long id);
    CityManager add(String name);
    CityManager update(Long id, String name);
    void remove(Long id);
}
