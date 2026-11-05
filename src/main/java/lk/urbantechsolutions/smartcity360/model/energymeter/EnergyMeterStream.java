package lk.urbantechsolutions.smartcity360.model.energymeter;

/** Component shared by the meter and all decorators. */
public interface EnergyMeterStream {
    byte[] readData();
}
