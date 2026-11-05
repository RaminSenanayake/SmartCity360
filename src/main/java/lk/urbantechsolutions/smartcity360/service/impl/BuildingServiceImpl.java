package lk.urbantechsolutions.smartcity360.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lk.urbantechsolutions.smartcity360.model.zone.Building;
import lk.urbantechsolutions.smartcity360.model.zone.Street;
import lk.urbantechsolutions.smartcity360.repository.BuildingRepository;
import lk.urbantechsolutions.smartcity360.repository.StreetRepository;
import lk.urbantechsolutions.smartcity360.service.BuildingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BuildingServiceImpl implements BuildingService {

    private final BuildingRepository repository;
    private final StreetRepository streetRepository;

    public BuildingServiceImpl(BuildingRepository repository, StreetRepository streetRepository) {
        this.repository = repository;
        this.streetRepository = streetRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Building> getByStreet(Long streetId) {
        return repository.findByStreet_Id(streetId);
    }

    @Override
    @Transactional(readOnly = true)
    public Building get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Building " + id + " not found"));
    }

    @Override
    @Transactional
    public Building add(Long streetId, String name, BigDecimal powerUsage) {
        validate(name, powerUsage);
        Street street = findStreet(streetId);
        Building building = new Building(name.trim(), powerUsage);
        street.addBuilding(building);
        return repository.save(building);
    }

    @Override
    @Transactional
    public Building update(Long id, String name, BigDecimal powerUsage) {
        validate(name, powerUsage);
        Building building = get(id);
        building.setName(name.trim());
        building.setPowerUsage(powerUsage);   // add this setter if Building doesn't have one
        return building;
    }

    @Override
    @Transactional
    public void remove(Long id) {
        Building building = get(id);
        Street street = building.getStreet();   // add this getter if missing
        if (street != null) {
            street.removeBuilding(building);    // orphanRemoval deletes the row
        } else {
            repository.delete(building);
        }
    }

    private Street findStreet(Long id) {
        return streetRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Street " + id + " not found"));
    }

    private void validate(String name, BigDecimal usage) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name must not be empty");
        if (usage == null || usage.signum() < 0) throw new IllegalArgumentException("Power usage must be zero or more");
    }
}
