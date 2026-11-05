package lk.urbantechsolutions.smartcity360.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lk.urbantechsolutions.smartcity360.model.role.CityManager;
import lk.urbantechsolutions.smartcity360.model.role.Supervisor;
import lk.urbantechsolutions.smartcity360.repository.CityManagerRepository;
import lk.urbantechsolutions.smartcity360.repository.SupervisorRepository;
import lk.urbantechsolutions.smartcity360.service.SupervisorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SupervisorServiceImpl implements SupervisorService {

    private final SupervisorRepository repository;
    private final CityManagerRepository cityManagerRepository;

    public SupervisorServiceImpl(SupervisorRepository repository,
                                 CityManagerRepository cityManagerRepository) {
        this.repository = repository;
        this.cityManagerRepository = cityManagerRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Supervisor> getAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Supervisor> getByCityManager(Long cityManagerId) {
        return repository.findByCityManager_Id(cityManagerId);
    }

    @Override
    @Transactional(readOnly = true)
    public Supervisor get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Supervisor " + id + " not found"));
    }

    @Override
    @Transactional
    public Supervisor add(String name, Long cityManagerId) {
        requireName(name);
        Supervisor supervisor = new Supervisor();
        supervisor.setName(name.trim());
        supervisor.setCityManager(findManager(cityManagerId));
        return repository.save(supervisor);
    }

    @Override
    @Transactional
    public Supervisor update(Long id, String name, Long cityManagerId) {
        requireName(name);
        Supervisor supervisor = get(id);
        supervisor.setName(name.trim());
        supervisor.setCityManager(findManager(cityManagerId));
        return supervisor;
    }

    @Override
    @Transactional
    public void remove(Long id) {
        repository.delete(get(id));   // cascades to their local staff
    }

    private CityManager findManager(Long id) {
        return cityManagerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("City manager " + id + " not found"));
    }

    private void requireName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name must not be empty");
    }
}
