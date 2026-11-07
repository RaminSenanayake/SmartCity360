package lk.urbantechsolutions.smartcity360.repository;

import lk.urbantechsolutions.smartcity360.model.account.PortalAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PortalAccountRepository extends JpaRepository<PortalAccount, Long> {
    Optional<PortalAccount> findByUsername(String username);
}
