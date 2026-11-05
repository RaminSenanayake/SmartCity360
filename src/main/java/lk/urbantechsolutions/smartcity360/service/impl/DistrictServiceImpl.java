package lk.urbantechsolutions.smartcity360.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lk.urbantechsolutions.smartcity360.model.zone.City;
import lk.urbantechsolutions.smartcity360.model.zone.District;
import lk.urbantechsolutions.smartcity360.repository.CityRepository;
import lk.urbantechsolutions.smartcity360.repository.DistrictRepository;
import lk.urbantechsolutions.smartcity360.service.DistrictService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DistrictServiceImpl implements DistrictService {

    private final DistrictRepository repository;
    private final CityRepository cityRepository;

    public DistrictServiceImpl(DistrictRepository repository, CityRepository cityRepository) {
        this.repository = repository;
        this.cityRepository = cityRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<District> getByCity(Long cityId) {
        return repository.findByCity_Id(cityId);
    }

    @Override
    @Transactional(readOnly = true)
    public District get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("District " + id + " not found"));
    }

    @Override
    @Transactional
    public District add(Long cityId, String name) {
        requireName(name);
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new EntityNotFoundException("City " + cityId + " not found"));
        District district = new District(name.trim());
        city.addDistrict(district);   // keeps both sides in sync; cascade saves it
        return repository.save(district);
    }

    @Override
    @Transactional
    public District update(Long id, String name) {
        requireName(name);
        District district = get(id);
        district.setName(name.trim());
        return district;
    }

    @Override
    @Transactional
    public void remove(Long id) {
        repository.delete(get(id));
    }

    private void requireName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name must not be empty");
    }
}
