package lk.urbantechsolutions.smartcity360.service;

import jakarta.persistence.EntityNotFoundException;
import lk.urbantechsolutions.smartcity360.model.policy.*;
import lk.urbantechsolutions.smartcity360.repository.CityPolicyRepository;
import lk.urbantechsolutions.smartcity360.service.impl.CityPolicyServiceImpl;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CityPolicyServiceTests {
    private final CityPolicyRepository repository = mock(CityPolicyRepository.class);
    private final CityPolicyService service = new CityPolicyServiceImpl(repository);
    private final PolicyContext context = new PolicyContext(new BigDecimal("50"), LocalTime.NOON);

    @Test
    void invalidCreateNeverWritesAPolicy() {
        assertThrows(PolicySyntaxException.class, () -> service.add("Noise", "NOISE = 55"));
        assertThrows(IllegalArgumentException.class, () -> service.add(" ", "NOISE < 55"));
        verifyNoInteractions(repository);
    }

    @Test
    void invalidUpdateDoesNotChangeExistingPolicy() {
        CityPolicy policy = new CityPolicy("Original", "NOISE <= 55");
        when(repository.findById(1L)).thenReturn(Optional.of(policy));
        assertThrows(PolicySyntaxException.class, () -> service.update(1L, "Changed", "invalid"));
        assertEquals("Original", policy.getName());
        assertEquals("NOISE <= 55", policy.getRule());
    }

    @Test
    void evaluationUsesUpdatedRuleWithoutRestartOrStaleCache() {
        CityPolicy policy = new CityPolicy("Noise", "NOISE <= 55");
        when(repository.findById(1L)).thenReturn(Optional.of(policy));
        assertTrue(service.evaluate(1L, context));
        service.update(1L, " Stricter noise ", " NOISE <= 45 ");
        assertEquals("Stricter noise", policy.getName());
        assertFalse(service.evaluate(1L, context));
    }

    @Test
    void missingPoliciesAndMissingContextAreReported() {
        assertThrows(EntityNotFoundException.class, () -> service.evaluate(99L, context));
        assertThrows(NullPointerException.class, () -> service.preview("NOISE < 55", null));
        assertTrue(service.preview("NOISE < 55", context));
    }
}
