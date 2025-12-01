package com.berti.data;

public final class SampleEventDataSetter implements DataSetter<SampleEvent> {

    @Override
    public void copyData(SampleEvent source, SampleEvent target) {
        target.setCreationTime(source.getCreationTime());
        target.setReceptionTime(source.getReceptionTime());
        target.setProducerId(source.getProducerId());
        target.setEventNumber(source.getEventNumber());
        target.setValue(source.getValue());
    }
}
