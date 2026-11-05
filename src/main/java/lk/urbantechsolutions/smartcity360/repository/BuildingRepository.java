package lk.urbantechsolutions.smartcity360.repository;

import lk.urbantechsolutions.smartcity360.model.zone.Building;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BuildingRepository extends JpaRepository<Building, Long> {
    List<Building> findByStreet_Id(Long streetId);
}
