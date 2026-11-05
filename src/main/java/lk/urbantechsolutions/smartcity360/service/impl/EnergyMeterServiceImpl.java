package lk.urbantechsolutions.smartcity360.service.impl;

import lk.urbantechsolutions.smartcity360.model.energymeter.AuthenticationDecorator;
import lk.urbantechsolutions.smartcity360.model.energymeter.BuildingEnergyMeter;
import lk.urbantechsolutions.smartcity360.model.energymeter.CompressionDecorator;
import lk.urbantechsolutions.smartcity360.model.energymeter.EncryptionDecorator;
import lk.urbantechsolutions.smartcity360.model.energymeter.EnergyMeterStream;
import lk.urbantechsolutions.smartcity360.service.BuildingService;
import lk.urbantechsolutions.smartcity360.service.EnergyMeterService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Service
public class EnergyMeterServiceImpl implements EnergyMeterService {
    private final BuildingService buildingService;
    private final String accessToken;
    private final String encryptionKey;

    public EnergyMeterServiceImpl(BuildingService buildingService,
                                 @Value("${energy-meter.access-token}") String accessToken,
                                 @Value("${energy-meter.encryption-key}") String encryptionKey) {
        this.buildingService = buildingService;
        this.accessToken = accessToken;
        this.encryptionKey = encryptionKey;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] readData(Long buildingId, String token, boolean compress, boolean encrypt) {
        // Defer the database lookup until the outer authentication decorator permits access.
        EnergyMeterStream stream = () -> new BuildingEnergyMeter(buildingService.get(buildingId)).readData();
        if (compress) {
            stream = new CompressionDecorator(stream);
        }
        if (encrypt) {
            stream = new EncryptionDecorator(stream, getEncryptionKey());
        }
        stream = new AuthenticationDecorator(stream, token, accessToken);
        return stream.readData();
    }

    private SecretKey getEncryptionKey() {
        if (encryptionKey == null || encryptionKey.isBlank()) {
            throw new IllegalStateException("Energy meter encryption key is not configured");
        }
        byte[] decoded = Base64.getDecoder().decode(encryptionKey);
        if (decoded.length != 16 && decoded.length != 24 && decoded.length != 32) {
            throw new IllegalArgumentException("Energy meter AES key must contain 16, 24 or 32 bytes");
        }
        return new SecretKeySpec(decoded, "AES");
    }
}
