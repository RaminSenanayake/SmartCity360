package lk.urbantechsolutions.smartcity360.repository;

import lk.urbantechsolutions.smartcity360.model.role.Supervisor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupervisorRepository extends JpaRepository<Supervisor, Long> {
    List<Supervisor> findByCityManager_Id(Long cityManagerId);
}
