package lk.urbantechsolutions.smartcity360.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lk.urbantechsolutions.smartcity360.model.role.CityManager;
import lk.urbantechsolutions.smartcity360.repository.CityManagerRepository;
import lk.urbantechsolutions.smartcity360.service.CityManagerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CityManagerServiceImpl implements CityManagerService {

    private final CityManagerRepository repository;

    public CityManagerServiceImpl(CityManagerRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CityManager> getAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public CityManager get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("City manager " + id + " not found"));
    }

    @Override
    @Transactional
    public CityManager add(String name) {
        requireName(name);
        CityManager manager = new CityManager();
        manager.setName(name.trim());
        return repository.save(manager);
    }

    @Override
    @Transactional
    public CityManager update(Long id, String name) {
        requireName(name);
        CityManager manager = get(id);
        manager.setName(name.trim());
        return manager;
    }

    @Override
    @Transactional
    public void remove(Long id) {
        repository.delete(get(id));   // cascades to supervisors and their local staff
    }

    private void requireName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name must not be empty");
    }
}
