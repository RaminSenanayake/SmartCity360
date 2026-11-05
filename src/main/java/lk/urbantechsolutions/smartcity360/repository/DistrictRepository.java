package lk.urbantechsolutions.smartcity360.repository;

import lk.urbantechsolutions.smartcity360.model.zone.District;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DistrictRepository extends JpaRepository<District, Long> {
    List<District> findByCity_Id(Long cityId);
}
