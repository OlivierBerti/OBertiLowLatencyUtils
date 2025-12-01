package com.berti.statistics.data;

import com.berti.data.DataSetter;

public class MeasurementPackDataSetter implements DataSetter<MeasurementPack> {
    @Override
    public void copyData(MeasurementPack source, MeasurementPack target) {
        source.copyTo(target);
    }
}
