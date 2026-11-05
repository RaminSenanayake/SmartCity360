package lk.urbantechsolutions.smartcity360.model.energymeter;

import lk.urbantechsolutions.smartcity360.model.zone.Building;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;

/** Simulates a meter using the building's stored usage; no hardware is connected. */
public class BuildingEnergyMeter implements EnergyMeterStream {
    private final Building building;

    public BuildingEnergyMeter(Building building) {
        this.building = Objects.requireNonNull(building);
    }

    @Override
    public byte[] readData() {
        String reading = "{\"buildingId\":" + building.getId()
                + ",\"powerOn\":" + building.isPowerOn()
                + ",\"powerUsage\":" + building.getPowerUsage().toPlainString()
                + ",\"readAt\":\"" + Instant.now() + "\"}";
        return reading.getBytes(StandardCharsets.UTF_8);
    }
}
