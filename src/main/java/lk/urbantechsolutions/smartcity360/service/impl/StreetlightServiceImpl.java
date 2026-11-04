package lk.urbantechsolutions.smartcity360.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lk.urbantechsolutions.smartcity360.model.streetlight.LightDistribution;
import lk.urbantechsolutions.smartcity360.model.streetlight.SharedStreetlight;
import lk.urbantechsolutions.smartcity360.model.streetlight.Streetlight;
import lk.urbantechsolutions.smartcity360.model.streetlight.StreetlightFactory;
import lk.urbantechsolutions.smartcity360.repository.StreetRepository;
import lk.urbantechsolutions.smartcity360.repository.StreetlightRepository;
import lk.urbantechsolutions.smartcity360.service.StreetlightService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class StreetlightServiceImpl implements StreetlightService {

    private final StreetlightRepository streetlightRepository;
    private final StreetRepository streetRepository;
    private final StreetlightFactory streetlightFactory;

    public StreetlightServiceImpl(StreetlightRepository streetlightRepository,
                                  StreetRepository streetRepository,
                                  StreetlightFactory streetlightFactory) {
        this.streetlightRepository = streetlightRepository;
        this.streetRepository = streetRepository;
        this.streetlightFactory = streetlightFactory;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Streetlight> getStreetlights(Long streetId) {
        return streetlightRepository.getAllByStreet_Id(streetId);
    }

    @Override
    @Transactional(readOnly = true)
    public Streetlight getStreetlight(Long id) {
        return streetlightRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Streetlight " + id + " not found"));
    }

    @Override
    @Transactional
    public void addStreetlight(Long streetId, BigDecimal longitude, BigDecimal latitude,
                               BigDecimal wattage, String color, int colorTemperature,
                               LightDistribution distribution) {
        SharedStreetlight shared =
                streetlightFactory.getSharedStreetlight(wattage, color, colorTemperature, distribution);

        Streetlight light = new Streetlight(longitude, latitude, shared);
        light.setStreet(streetRepository.getReferenceById(streetId));
        streetlightRepository.save(light);
    }

    @Override
    @Transactional
    public void updateStreetlight(Streetlight streetlight) {
        Streetlight existing = getStreetlight(streetlight.getId());
        existing.setLongitude(streetlight.getLongitude());
        existing.setLatitude(streetlight.getLatitude());
        existing.setStreet(streetlight.getStreet());
        existing.setSharedStreetlight(streetlight.getSharedStreetlight());
    }

    @Override
    @Transactional
    public void removeStreetlight(Streetlight streetlight) {
        streetlightRepository.deleteById(streetlight.getId());
    }
}
