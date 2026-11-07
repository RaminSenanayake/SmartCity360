package lk.urbantechsolutions.smartcity360.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

/** Captures an explicit, unambiguous portal selection from the sign-in form. */
public class PortalAuthenticationDetails extends WebAuthenticationDetails {
    private final String portal;

    public PortalAuthenticationDetails(HttpServletRequest request) {
        super(request);
        String[] values = request.getParameterValues("portal");
        portal = values != null && values.length == 1
                && ("resident".equals(values[0]) || "staff".equals(values[0])) ? values[0] : null;
    }

    public String getPortal() { return portal; }
}
