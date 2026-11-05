package lk.urbantechsolutions.smartcity360.repository;

import lk.urbantechsolutions.smartcity360.model.zone.Street;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StreetRepository extends JpaRepository<Street,Long> {
    List<Street> findByDistrict_Id(Long districtId);
}
