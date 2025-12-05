package com.berti.eventbus.multithread;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class EventBusClientConfiguration {

    private final long tempoInNanos;

    private final boolean conflating;

    public EventBusClientConfiguration(long tempoInNanos, boolean conflating) {
        this.tempoInNanos = tempoInNanos;
        this.conflating = conflating;
    }
}
