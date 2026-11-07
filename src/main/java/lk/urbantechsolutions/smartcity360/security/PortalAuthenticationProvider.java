package lk.urbantechsolutions.smartcity360.security;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

/** Validates the portal against the stored account before authentication can succeed. */
public class PortalAuthenticationProvider extends DaoAuthenticationProvider {
    public PortalAuthenticationProvider(UserDetailsService users) {
        super(users);
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
                                                   UsernamePasswordAuthenticationToken authentication)
            throws AuthenticationException {
        super.additionalAuthenticationChecks(userDetails, authentication);
        if (!(authentication.getDetails() instanceof PortalAuthenticationDetails details)
                || details.getPortal() == null
                || !(userDetails instanceof PortalUser user)
                || user.isStaff() != "staff".equals(details.getPortal())) {
            throw new BadCredentialsException("Invalid credentials or portal");
        }
    }
}
