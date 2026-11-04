package lk.urbantechsolutions.smartcity360.repository;

import lk.urbantechsolutions.smartcity360.model.streetlight.Streetlight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StreetlightRepository extends JpaRepository<Streetlight, Long> {
    List<Streetlight> getAllByStreet_Id(Long streetId);
}
