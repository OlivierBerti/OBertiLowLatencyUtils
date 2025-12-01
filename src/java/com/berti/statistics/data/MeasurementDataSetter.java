package com.berti.statistics.data;

import com.berti.data.DataSetter;

public final class MeasurementDataSetter implements DataSetter<Measurement> {

    @Override
    public void copyData(Measurement source, Measurement target) {
        target.setValue(source.getValue());
        target.setTimestamsp(source.getTimestamsp());
    }
}
