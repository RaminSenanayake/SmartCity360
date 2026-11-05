package lk.urbantechsolutions.smartcity360.model.energymeter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.zip.GZIPOutputStream;

public class CompressionDecorator extends EnergyMeterStreamDecorator {
    public CompressionDecorator(EnergyMeterStream wrapped) {
        super(wrapped);
    }

    @Override
    public byte[] readData() {
        byte[] data = super.readData();
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try (GZIPOutputStream gzip = new GZIPOutputStream(output)) {
                gzip.write(data);
            }
            return output.toByteArray();
        } catch (IOException exception) {
            throw new UncheckedIOException("Cannot compress meter reading", exception);
        }
    }
}
