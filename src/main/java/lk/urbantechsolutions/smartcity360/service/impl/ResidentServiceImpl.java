package lk.urbantechsolutions.smartcity360.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lk.urbantechsolutions.smartcity360.model.resident.Resident;
import lk.urbantechsolutions.smartcity360.repository.ResidentRepository;
import lk.urbantechsolutions.smartcity360.service.ResidentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ResidentServiceImpl implements ResidentService {

    private final ResidentRepository residentRepository;

    public ResidentServiceImpl(ResidentRepository residentRepository) {
        this.residentRepository = residentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Resident> getAllResidents() {
        return residentRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Resident getResident(Long id) {
        return residentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Resident " + id + " not found"));
    }

    @Override
    @Transactional
    public Resident addResident(String name) {
        validateName(name);
        return residentRepository.save(new Resident(name.trim()));
    }

    @Override
    @Transactional
    public Resident updateResident(Long id, String name) {
        validateName(name);
        Resident resident = getResident(id);
        resident.setName(name.trim());
        return resident;   // managed entity: changes flush on commit, no save() needed
    }

    @Override
    @Transactional
    public void removeResident(Long id) {
        Resident resident = getResident(id);
        residentRepository.delete(resident);
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Resident name must not be empty");
        }
    }
}