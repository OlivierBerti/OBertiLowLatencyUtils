package com.berti.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public final class SampleEvent {

    private long creationTime;

    private long receptionTime;

    private String producerId;

    private int eventNumber;

    private int value;

    public long getLatency() {
        return receptionTime - creationTime;
    }
}
