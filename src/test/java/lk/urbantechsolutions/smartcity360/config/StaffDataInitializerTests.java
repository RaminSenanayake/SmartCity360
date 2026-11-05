package lk.urbantechsolutions.smartcity360.config;

import lk.urbantechsolutions.smartcity360.model.role.StaffIdentity;
import lk.urbantechsolutions.smartcity360.model.role.StaffType;
import lk.urbantechsolutions.smartcity360.service.StaffAuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StaffDataInitializerTests {
    @Autowired private StaffAuthenticationService authentication;
    @Autowired @Qualifier("seedStaff") private CommandLineRunner seeder;
    @Value("${seed.staff-token-file:.seed/staff-access-tokens.properties}") private String tokenFile;

    @Test
    void exportedTokensAuthenticateAllSeededStaffAndRerunPreservesThem() throws Exception {
        Path file = Path.of(tokenFile);
        byte[] original = Files.readAllBytes(file);
        Properties tokens = new Properties();
        try (InputStream input = Files.newInputStream(file)) {
            tokens.load(input);
        }
        assertEquals(28, tokens.size());
        assertEquals(28, new HashSet<>(tokens.values()).size());
        for (String identity : tokens.stringPropertyNames()) {
            String[] parts = identity.split("\\.");
            StaffIdentity staff = new StaffIdentity(StaffType.valueOf(parts[0]), Long.valueOf(parts[1]));
            assertDoesNotThrow(() -> authentication.authenticate(staff, tokens.getProperty(identity)));
        }
        for (StaffType type : StaffType.values()) {
            long expectedCount = switch (type) {
                case CITY_MANAGER -> 4;
                case SUPERVISOR -> 8;
                case LOCAL_STAFF -> 16;
            };
            assertEquals(expectedCount,
                    tokens.stringPropertyNames().stream().filter(key -> key.startsWith(type.name() + ".")).count());
        }
        seeder.run();
        assertArrayEquals(original, Files.readAllBytes(file));
        for (String identity : tokens.stringPropertyNames()) {
            String[] parts = identity.split("\\.");
            authentication.authenticate(new StaffIdentity(StaffType.valueOf(parts[0]), Long.valueOf(parts[1])),
                    tokens.getProperty(identity));
        }
    }
}
