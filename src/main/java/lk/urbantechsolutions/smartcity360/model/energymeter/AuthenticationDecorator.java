package lk.urbantechsolutions.smartcity360.model.energymeter;

import lk.urbantechsolutions.smartcity360.model.role.StaffIdentity;
import lk.urbantechsolutions.smartcity360.service.StaffAuthenticationService;

import java.util.Objects;

/** Authenticates the staff account before invoking the wrapped stream on every read. */
public class AuthenticationDecorator extends EnergyMeterStreamDecorator {
    private final StaffAuthenticationService authenticationService;
    private final StaffIdentity identity;
    private final String token;

    public AuthenticationDecorator(EnergyMeterStream wrapped,
                                   StaffAuthenticationService authenticationService,
                                   StaffIdentity identity, String token) {
        super(wrapped);
        this.authenticationService = Objects.requireNonNull(authenticationService);
        this.identity = identity;
        this.token = token;
    }

    @Override
    public byte[] readData() {
        authenticationService.authenticate(identity, token);
        return super.readData();
    }
}
