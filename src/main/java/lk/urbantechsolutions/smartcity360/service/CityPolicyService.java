package lk.urbantechsolutions.smartcity360.service;

import lk.urbantechsolutions.smartcity360.model.policy.CityPolicy;
import lk.urbantechsolutions.smartcity360.model.policy.PolicyContext;
import java.util.List;

public interface CityPolicyService {
    List<CityPolicy> getAll();
    CityPolicy get(Long id);
    CityPolicy add(String name, String rule);
    CityPolicy update(Long id, String name, String rule);
    void remove(Long id);
    boolean evaluate(Long id, PolicyContext context);
    boolean preview(String rule, PolicyContext context);
}
