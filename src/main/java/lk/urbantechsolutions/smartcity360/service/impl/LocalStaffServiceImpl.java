package lk.urbantechsolutions.smartcity360.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lk.urbantechsolutions.smartcity360.model.role.LocalStaff;
import lk.urbantechsolutions.smartcity360.model.role.Supervisor;
import lk.urbantechsolutions.smartcity360.repository.LocalStaffRepository;
import lk.urbantechsolutions.smartcity360.repository.SupervisorRepository;
import lk.urbantechsolutions.smartcity360.service.LocalStaffService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LocalStaffServiceImpl implements LocalStaffService {

    private final LocalStaffRepository repository;
    private final SupervisorRepository supervisorRepository;

    public LocalStaffServiceImpl(LocalStaffRepository repository,
                                 SupervisorRepository supervisorRepository) {
        this.repository = repository;
        this.supervisorRepository = supervisorRepository;
    }

    @Override @Transactional(readOnly = true)
    public List<LocalStaff> getAll() { return repository.findAll(); }

    @Override @Transactional(readOnly = true)
    public List<LocalStaff> getBySupervisor(Long supervisorId) {
        return repository.findBySupervisor_Id(supervisorId);
    }

    @Override @Transactional(readOnly = true)
    public LocalStaff get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Local staff " + id + " not found"));
    }

    @Override @Transactional
    public LocalStaff add(String name, Long supervisorId) {
        requireName(name);
        LocalStaff staff = new LocalStaff();
        staff.setName(name.trim());
        staff.setSupervisor(findSupervisor(supervisorId));
        return repository.save(staff);
    }

    @Override @Transactional
    public LocalStaff update(Long id, String name, Long supervisorId) {
        requireName(name);
        LocalStaff staff = get(id);
        staff.setName(name.trim());
        staff.setSupervisor(findSupervisor(supervisorId));
        return staff;
    }

    @Override @Transactional
    public void remove(Long id) {
        repository.delete(get(id));
    }

    private Supervisor findSupervisor(Long id) {
        return supervisorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Supervisor " + id + " not found"));
    }

    private void requireName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name must not be empty");
    }
}