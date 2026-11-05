package lk.urbantechsolutions.smartcity360.model.energymeter;

import lk.urbantechsolutions.smartcity360.model.zone.Building;
import lk.urbantechsolutions.smartcity360.service.BuildingService;
import lk.urbantechsolutions.smartcity360.service.impl.EnergyMeterServiceImpl;
import org.junit.jupiter.api.Test;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EnergyMeterStreamTests {
    private final SecretKeySpec key = new SecretKeySpec(new byte[32], "AES");

    @Test
    void buildingMeterReadsCurrentBuildingState() {
        Building building = new Building("Office", new BigDecimal("123.45"));
        EnergyMeterStream meter = new BuildingEnergyMeter(building);
        assertTrue(new String(meter.readData(), StandardCharsets.UTF_8).contains("\"powerUsage\":123.45"));
        building.setPowerUsage(new BigDecimal("200.00"));
        building.turnOnPower();
        String reading = new String(meter.readData(), StandardCharsets.UTF_8);
        assertTrue(reading.contains("\"powerUsage\":200.00"));
        assertTrue(reading.contains("\"powerOn\":true"));
    }

    @Test
    void compressionCanBeUsedAlone() throws Exception {
        byte[] original = "meter reading".getBytes(StandardCharsets.UTF_8);
        byte[] compressed = new CompressionDecorator(() -> original).readData();
        assertArrayEquals(original, decompress(compressed));
    }

    @Test
    void allDecoratorsComposeAndReadingCanBeRecovered() throws Exception {
        byte[] original = "meter reading".getBytes(StandardCharsets.UTF_8);
        EnergyMeterStream stream = new AuthenticationDecorator(
                new EncryptionDecorator(new CompressionDecorator(() -> original), key), "secret", "secret");
        assertArrayEquals(original, decompress(decrypt(stream.readData())));
    }

    @Test
    void encryptionUsesFreshNonceAndDetectsTampering() throws Exception {
        byte[] original = "meter reading".getBytes(StandardCharsets.UTF_8);
        EnergyMeterStream stream = new EncryptionDecorator(() -> original, key);
        byte[] first = stream.readData();
        byte[] second = stream.readData();
        assertFalse(Arrays.equals(first, second));
        assertArrayEquals(original, decrypt(first));
        assertArrayEquals(original, decrypt(second));
        first[first.length - 1] ^= 1;
        assertThrows(AEADBadTagException.class, () -> decrypt(first));
    }

    @Test
    void invalidTokenNeverInvokesWrappedStream() {
        EnergyMeterStream source = mock(EnergyMeterStream.class);
        for (String token : new String[]{null, "", "wrong"}) {
            EnergyMeterStream stream = new AuthenticationDecorator(source, token, "secret");
            assertThrows(SecurityException.class, stream::readData);
        }
        verifyNoInteractions(source);
        assertThrows(IllegalStateException.class, () -> new AuthenticationDecorator(source, "", ""));
    }

    @Test
    void serviceReadsBuildingWithEveryOptionalLayerCombination() throws Exception {
        BuildingService buildings = mock(BuildingService.class);
        when(buildings.get(7L)).thenReturn(new Building("Office", new BigDecimal("123.45")));
        EnergyMeterServiceImpl service = new EnergyMeterServiceImpl(buildings, "secret",
                Base64.getEncoder().encodeToString(key.getEncoded()));
        for (boolean compress : new boolean[]{false, true}) {
            for (boolean encrypt : new boolean[]{false, true}) {
                byte[] data = service.readData(7L, "secret", compress, encrypt);
                if (encrypt) data = decrypt(data);
                if (compress) data = decompress(data);
                assertTrue(new String(data, StandardCharsets.UTF_8).contains("\"powerUsage\":123.45"));
            }
        }
        verify(buildings, times(4)).get(7L);
    }

    @Test
    void serviceRejectsUnauthenticatedReadsBeforeBuildingLookup() {
        BuildingService buildings = mock(BuildingService.class);
        EnergyMeterServiceImpl service = new EnergyMeterServiceImpl(buildings, "secret", "");
        assertThrows(SecurityException.class, () -> service.readData(7L, "wrong", false, false));
        verifyNoInteractions(buildings);
    }

    @Test
    void serviceRequiresConfiguredSecrets() {
        BuildingService buildings = mock(BuildingService.class);
        EnergyMeterServiceImpl unconfigured = new EnergyMeterServiceImpl(buildings, "", "");
        assertThrows(IllegalStateException.class, () -> unconfigured.readData(7L, "", false, false));
        EnergyMeterServiceImpl missingKey = new EnergyMeterServiceImpl(buildings, "secret", "");
        assertThrows(IllegalStateException.class, () -> missingKey.readData(7L, "secret", false, true));
        EnergyMeterServiceImpl shortKey = new EnergyMeterServiceImpl(buildings, "secret", "YQ==");
        assertThrows(IllegalArgumentException.class, () -> shortKey.readData(7L, "secret", false, true));
        verifyNoInteractions(buildings);
    }

    private byte[] decrypt(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, Arrays.copyOf(data, 12)));
        return cipher.doFinal(Arrays.copyOfRange(data, 12, data.length));
    }

    private byte[] decompress(byte[] data) throws Exception {
        try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(data))) {
            return gzip.readAllBytes();
        }
    }
}
