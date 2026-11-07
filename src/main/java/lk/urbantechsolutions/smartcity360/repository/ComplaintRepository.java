package lk.urbantechsolutions.smartcity360.repository;

import lk.urbantechsolutions.smartcity360.model.resident.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    java.util.List<Complaint> findByResident_IdOrderByCreatedAtDesc(Long residentId);
}
