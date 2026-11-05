package lk.urbantechsolutions.smartcity360.model.energymeter;

import java.util.Objects;

/** Base decorator: wraps any component, including another decorator. */
public abstract class EnergyMeterStreamDecorator implements EnergyMeterStream {
    protected final EnergyMeterStream wrapped;

    protected EnergyMeterStreamDecorator(EnergyMeterStream wrapped) {
        this.wrapped = Objects.requireNonNull(wrapped);
    }

    @Override
    public byte[] readData() {
        return wrapped.readData();
    }
}
