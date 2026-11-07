package lk.urbantechsolutions.smartcity360.config;

import lk.urbantechsolutions.smartcity360.model.account.PortalAccount;
import lk.urbantechsolutions.smartcity360.model.role.Staff;
import lk.urbantechsolutions.smartcity360.model.role.StaffType;
import lk.urbantechsolutions.smartcity360.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;
import java.nio.file.*;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Properties;

@Configuration
public class PortalAccountDataInitializer {
    @Bean @Order(6)
    CommandLineRunner seedPortalAccounts(PortalAccountRepository accounts, ResidentRepository residents,
                                        LocalStaffRepository locals, SupervisorRepository supervisors,
                                        CityManagerRepository managers, PasswordEncoder encoder,
                                        TransactionTemplate transactionTemplate,
                                        @Value("${seed.portal-account-file:.seed/portal-accounts.properties}") String credentialFile) {
        return args -> {
            Properties credentials = transactionTemplate.execute(status -> {
                Properties result = new Properties();
                residents.findAll().forEach(resident -> provision(accounts, encoder, result,
                        "resident." + resident.getId(), resident.getName(), null, resident.getId()));
                locals.findAll().forEach(staff -> provisionStaff(accounts, encoder, result, StaffType.LOCAL_STAFF, staff));
                supervisors.findAll().forEach(staff -> provisionStaff(accounts, encoder, result, StaffType.SUPERVISOR, staff));
                managers.findAll().forEach(staff -> provisionStaff(accounts, encoder, result, StaffType.CITY_MANAGER, staff));
                return result;
            });
            if (credentials != null && !credentials.isEmpty()) {
                Path file = Path.of(credentialFile);
                Files.createDirectories(file.toAbsolutePath().getParent());
                // Preserve earlier credentials if new demo accounts are added on a later startup.
                Properties all = new Properties();
                if (Files.exists(file)) try (var input = Files.newInputStream(file)) { all.load(input); }
                all.putAll(credentials);
                try (var output = Files.newOutputStream(file)) {
                    all.store(output, "Local portal sign-in: username=password. Keep private.");
                }
            }
        };
    }

    private void provisionStaff(PortalAccountRepository accounts, PasswordEncoder encoder, Properties output,
                                StaffType type, Staff staff) {
        provision(accounts, encoder, output, type.name().toLowerCase(java.util.Locale.ROOT) + "." + staff.getId(),
                staff.getName(), type, staff.getId());
    }

    private void provision(PortalAccountRepository accounts, PasswordEncoder encoder, Properties output,
                           String username, String name, StaffType type, Long id) {
        if (accounts.findByUsername(username).isPresent()) return;
        byte[] bytes = new byte[18];
        new SecureRandom().nextBytes(bytes);
        String password = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        accounts.save(new PortalAccount(username, encoder.encode(password), name, type, id));
        output.setProperty(username, password);
    }
}
