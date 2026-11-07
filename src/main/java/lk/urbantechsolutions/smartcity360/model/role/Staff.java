package lk.urbantechsolutions.smartcity360.model.role;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lk.urbantechsolutions.smartcity360.model.resident.Complaint;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.ArrayList;
import java.util.List;

@MappedSuperclass
public abstract class Staff {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(length = 64)
    private String accessTokenHash;

    /** Trusted provisioning code must persist this change and deliver the returned token privately. */
    public String rotateAccessToken() {
        byte[] random = new byte[32];
        new SecureRandom().nextBytes(random);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(random);
        accessTokenHash = hashToken(token);
        return token;
    }

    public void revokeAccessToken() {
        accessTokenHash = null;
    }

    public boolean hasValidAccessToken(String token) {
        return token != null && !token.isBlank() && accessTokenHash != null
                && MessageDigest.isEqual(accessTokenHash.getBytes(StandardCharsets.US_ASCII),
                hashToken(token).getBytes(StandardCharsets.US_ASCII));
    }

    private static String hashToken(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public final Complaint handleComplaint(Complaint complaint) {
        if (complaint.getSeverity() == null) {
            throw new IllegalStateException("A complaint must be assessed before routing");
        }
        return handle(complaint, new ArrayList<>());
    }

    protected Complaint handle(Complaint complaint, List<String> path) {
        path.add(getRole() + " " + getName());

        if (canHandle(complaint)) {
            complaint = process(complaint);
        } else if (getNextHandler() != null) {
            return getNextHandler().handle(complaint, path);
        }
        // if nobody can handle it, handled stays false

        complaint.setEscalationPath(String.join(" -> ", path));
        return complaint;
    }

    // default behaviour: record who handled it
    protected Complaint process(Complaint complaint) {
        complaint.markHandled(getRole(), getId(), getName());
        return complaint;
    }

    protected abstract String getRole();
    protected abstract boolean canHandle(Complaint complaint);
    protected abstract Staff getNextHandler();
}
