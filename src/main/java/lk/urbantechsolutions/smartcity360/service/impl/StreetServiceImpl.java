package lk.urbantechsolutions.smartcity360.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lk.urbantechsolutions.smartcity360.model.zone.District;
import lk.urbantechsolutions.smartcity360.model.zone.Street;
import lk.urbantechsolutions.smartcity360.repository.DistrictRepository;
import lk.urbantechsolutions.smartcity360.repository.StreetRepository;
import lk.urbantechsolutions.smartcity360.service.StreetService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StreetServiceImpl implements StreetService {

    private final StreetRepository repository;
    private final DistrictRepository districtRepository;

    public StreetServiceImpl(StreetRepository repository, DistrictRepository districtRepository) {
        this.repository = repository;
        this.districtRepository = districtRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Street> getByDistrict(Long districtId) {
        return repository.findByDistrict_Id(districtId);
    }

    @Override
    @Transactional(readOnly = true)
    public Street get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Street " + id + " not found"));
    }

    @Override
    @Transactional
    public Street add(Long districtId, String name) {
        requireName(name);
        District district = districtRepository.findById(districtId)
                .orElseThrow(() -> new EntityNotFoundException("District " + districtId + " not found"));
        Street street = new Street(name.trim());
        district.addStreet(street);
        return repository.save(street);
    }

    @Override
    @Transactional
    public Street update(Long id, String name) {
        requireName(name);
        Street street = get(id);
        street.setName(name.trim());
        return street;
    }

    @Override
    @Transactional
    public void remove(Long id) {
        repository.delete(get(id));   // cascades to buildings and streetlights
    }

    private void requireName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name must not be empty");
    }
}
