package lk.urbantechsolutions.smartcity360.repository;

import lk.urbantechsolutions.smartcity360.model.role.LocalStaff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocalStaffRepository extends JpaRepository<LocalStaff, Long> {
    List<LocalStaff> findBySupervisor_Id(Long supervisorId);
}
