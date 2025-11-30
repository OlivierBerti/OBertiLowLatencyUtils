package com.berti.eventbus.multithread;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class RingBufferConfiguration {

    private final int ringBufferSize;

    private final long tempoInNanos;

    private final boolean multiProducer;

    public RingBufferConfiguration(int ringBufferSize, long tempoInNanos, boolean multiProducer) {
        this.ringBufferSize = ringBufferSize;
        this.tempoInNanos = tempoInNanos;
        this.multiProducer = multiProducer;
    }
}
