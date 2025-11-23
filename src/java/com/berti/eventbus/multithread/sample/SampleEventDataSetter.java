package com.berti.eventbus.multithread.sample;

import com.berti.eventbus.DataSetter;

public class SampleEventDataSetter implements DataSetter<SampleEvent> {

    @Override
    public void copyData(SampleEvent source, SampleEvent target) {
        target.setCreationTime(source.getCreationTime());
        target.setReceptionTime(source.getReceptionTime());
        target.setProducerId(source.getProducerId());
        target.setEventNumber(source.getEventNumber());
        target.setValue(source.getValue());
    }
}
