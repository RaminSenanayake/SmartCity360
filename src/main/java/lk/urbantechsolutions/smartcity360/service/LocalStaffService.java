package lk.urbantechsolutions.smartcity360.service;

import lk.urbantechsolutions.smartcity360.model.role.LocalStaff;

import java.util.List;

public interface LocalStaffService {
    List<LocalStaff> getAll();
    List<LocalStaff> getBySupervisor(Long supervisorId);
    LocalStaff get(Long id);
    LocalStaff add(String name, Long supervisorId);
    LocalStaff update(Long id, String name, Long supervisorId);
    void remove(Long id);
}
