package lk.urbantechsolutions.smartcity360.service;

import lk.urbantechsolutions.smartcity360.model.role.StaffIdentity;

public interface StaffAuthenticationService {
    void authenticate(StaffIdentity identity, String token);
}
