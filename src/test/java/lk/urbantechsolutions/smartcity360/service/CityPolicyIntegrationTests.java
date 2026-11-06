package lk.urbantechsolutions.smartcity360.service;

import jakarta.persistence.EntityManager;
import lk.urbantechsolutions.smartcity360.model.policy.CityPolicy;
import lk.urbantechsolutions.smartcity360.model.policy.PolicyContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CityPolicyIntegrationTests {
    @Autowired private CityPolicyService policies;
    @Autowired private EntityManager entityManager;

    @Test
    @Transactional
    void policiesPersistAndUpdatedDslIsInterpretedAtRuntime() {
        PolicyContext context = new PolicyContext(new BigDecimal("50"), LocalTime.NOON);
        CityPolicy policy = policies.add("Test policy", "NOISE <= 55 AND PARKING BETWEEN 08:00 AND 18:00");
        Long id = policy.getId();
        entityManager.flush();
        entityManager.clear();
        assertTrue(policies.evaluate(id, context));
        policies.update(id, "Strict policy", "NOISE <= 45");
        entityManager.flush();
        entityManager.clear();
        assertFalse(policies.evaluate(id, context));
        policies.remove(id);
        entityManager.flush();
        assertFalse(policies.getAll().stream().anyMatch(item -> item.getId().equals(id)));
    }

    @Test
    void seededPoliciesDemonstrateNoiseParkingAndCombinedRules() {
        assertEquals(3, policies.getAll().size());
        for (CityPolicy policy : policies.getAll()) {
            assertDoesNotThrow(() -> policies.evaluate(policy.getId(),
                    new PolicyContext(new BigDecimal("40"), LocalTime.of(23, 0))));
        }
    }
}
