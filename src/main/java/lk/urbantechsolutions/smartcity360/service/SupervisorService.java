package lk.urbantechsolutions.smartcity360.service;

import lk.urbantechsolutions.smartcity360.model.role.Supervisor;

import java.util.List;

public interface SupervisorService {
    List<Supervisor> getAll();
    List<Supervisor> getByCityManager(Long cityManagerId);
    Supervisor get(Long id);
    Supervisor add(String name, Long cityManagerId);
    Supervisor update(Long id, String name, Long cityManagerId);
    void remove(Long id);
}
