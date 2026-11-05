package lk.urbantechsolutions.smartcity360.model.energymeter;

import lk.urbantechsolutions.smartcity360.model.zone.Building;
import lk.urbantechsolutions.smartcity360.repository.*;
import lk.urbantechsolutions.smartcity360.model.zone.ZoneType;
import java.util.Optional;
import lk.urbantechsolutions.smartcity360.service.StaffAuthenticationService;
import lk.urbantechsolutions.smartcity360.model.role.StaffIdentity;
import lk.urbantechsolutions.smartcity360.model.role.StaffType;
import lk.urbantechsolutions.smartcity360.service.impl.PowerControlServiceImpl;
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
    private final StaffAuthenticationService authentication = mock(StaffAuthenticationService.class);
    private final StaffIdentity staff = new StaffIdentity(StaffType.LOCAL_STAFF, 1L);
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
                new EncryptionDecorator(new CompressionDecorator(() -> original), key), authentication, staff, "secret");
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
        doThrow(new SecurityException("Invalid staff credentials"))
                .when(authentication).authenticate(staff, "wrong");
        EnergyMeterStream stream = new AuthenticationDecorator(source, authentication, staff, "wrong");
        assertThrows(SecurityException.class, stream::readData);
        verifyNoInteractions(source);
    }

    @Test
    void serviceReadsBuildingWithEveryOptionalLayerCombination() throws Exception {
        BuildingRepository buildings = mock(BuildingRepository.class);
        when(buildings.findById(7L)).thenReturn(Optional.of(new Building("Office", new BigDecimal("123.45"))));
        PowerControlServiceImpl service = new PowerControlServiceImpl(mock(CityRepository.class), mock(DistrictRepository.class),
                mock(StreetRepository.class), buildings, authentication,
                Base64.getEncoder().encodeToString(key.getEncoded()));
        for (boolean compress : new boolean[]{false, true}) {
            for (boolean encrypt : new boolean[]{false, true}) {
                byte[] data = service.getUsage(7L, ZoneType.BUILDING, staff, "secret", compress, encrypt);
                if (encrypt) data = decrypt(data);
                if (compress) data = decompress(data);
                assertTrue(new String(data, StandardCharsets.UTF_8).contains("\"powerUsage\":123.45"));
            }
        }
        var order = inOrder(authentication, buildings);
        for (int i = 0; i < 4; i++) {
            order.verify(authentication).authenticate(staff, "secret");
            order.verify(buildings).findById(7L);
        }
    }

    @Test
    void serviceRejectsUnauthenticatedReadsBeforeBuildingLookup() {
        BuildingRepository buildings = mock(BuildingRepository.class);
        PowerControlServiceImpl service = new PowerControlServiceImpl(mock(CityRepository.class), mock(DistrictRepository.class),
                mock(StreetRepository.class), buildings, authentication, "");
        doThrow(new SecurityException("Invalid staff credentials"))
                .when(authentication).authenticate(staff, "wrong");
        for (boolean compress : new boolean[]{false, true}) {
            for (boolean encrypt : new boolean[]{false, true}) {
                assertThrows(SecurityException.class,
                        () -> service.getUsage(7L, ZoneType.BUILDING, staff, "wrong", compress, encrypt));
            }
        }
        verifyNoInteractions(buildings);
    }

    @Test
    void serviceRequiresConfiguredEncryptionKeyAfterAuthentication() {
        BuildingRepository buildings = mock(BuildingRepository.class);
        PowerControlServiceImpl missingKey = new PowerControlServiceImpl(mock(CityRepository.class), mock(DistrictRepository.class),
                mock(StreetRepository.class), buildings, authentication, "");
        assertThrows(IllegalStateException.class, () -> missingKey.getUsage(7L, ZoneType.BUILDING, staff, "secret", false, true));
        PowerControlServiceImpl shortKey = new PowerControlServiceImpl(mock(CityRepository.class), mock(DistrictRepository.class),
                mock(StreetRepository.class), buildings, authentication, "YQ==");
        assertThrows(IllegalArgumentException.class, () -> shortKey.getUsage(7L, ZoneType.BUILDING, staff, "secret", false, true));
        verify(authentication, times(2)).authenticate(staff, "secret");
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
