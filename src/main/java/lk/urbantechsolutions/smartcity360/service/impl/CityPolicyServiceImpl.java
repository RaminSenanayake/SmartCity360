package lk.urbantechsolutions.smartcity360.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lk.urbantechsolutions.smartcity360.model.policy.*;
import lk.urbantechsolutions.smartcity360.repository.CityPolicyRepository;
import lk.urbantechsolutions.smartcity360.service.CityPolicyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class CityPolicyServiceImpl implements CityPolicyService {
    private final CityPolicyRepository repository;
    private final PolicyParser parser = new PolicyParser();

    public CityPolicyServiceImpl(CityPolicyRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<CityPolicy> getAll() { return repository.findAll(); }

    @Override
    public CityPolicy get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("City policy " + id + " not found"));
    }

    @Override
    @Transactional
    public CityPolicy add(String name, String rule) {
        validate(name, rule);
        return repository.save(new CityPolicy(name.trim(), rule.trim()));
    }

    @Override
    @Transactional
    public CityPolicy update(Long id, String name, String rule) {
        validate(name, rule);
        CityPolicy policy = get(id);
        policy.setName(name.trim());
        policy.setRule(rule.trim());
        return policy;
    }

    @Override
    @Transactional
    public void remove(Long id) { repository.delete(get(id)); }

    @Override
    public boolean evaluate(Long id, PolicyContext context) {
        return preview(get(id).getRule(), context);
    }

    @Override
    public boolean preview(String rule, PolicyContext context) {
        Objects.requireNonNull(context, "Policy context is required");
        return parser.parse(rule).interpret(context);
    }

    private void validate(String name, String rule) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Policy name is required");
        parser.parse(rule);
    }
}
