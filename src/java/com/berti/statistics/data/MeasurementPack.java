package com.berti.statistics.data;

import com.berti.data.DataArrayList;

import java.util.stream.Stream;

public class MeasurementPack {
    private final DataArrayList<Measurement> measurements ;

    public MeasurementPack(int capacity) {
        measurements = new DataArrayList<>(capacity, Measurement::new, new MeasurementDataSetter()) ;
    }

    public void addMeasurement(Measurement measurement) {
        measurements.addCopy(measurement);
    }

    public void clear() {
        measurements.clear();
    }

    public void copyTo(MeasurementPack target) {
        target.measurements.set(this.measurements);
    }

    public Stream<Integer> measurementsStream() {
        return measurements.stream().map(Measurement::getValue);
    }
}
