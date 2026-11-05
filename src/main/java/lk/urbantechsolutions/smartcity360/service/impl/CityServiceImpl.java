package lk.urbantechsolutions.smartcity360.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lk.urbantechsolutions.smartcity360.model.zone.City;
import lk.urbantechsolutions.smartcity360.repository.CityRepository;
import lk.urbantechsolutions.smartcity360.service.CityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CityServiceImpl implements CityService {

    private final CityRepository repository;

    public CityServiceImpl(CityRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<City> getAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public City get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("City " + id + " not found"));
    }

    @Override
    @Transactional
    public City add(String name) {
        requireName(name);
        return repository.save(new City(name.trim()));
    }

    @Override
    @Transactional
    public City update(Long id, String name) {
        requireName(name);
        City city = get(id);
        city.setName(name.trim());
        return city;
    }

    @Override
    @Transactional
    public void remove(Long id) {
        repository.delete(get(id));   // cascades down to districts, streets, buildings, streetlights
    }

    private void requireName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name must not be empty");
    }
}
