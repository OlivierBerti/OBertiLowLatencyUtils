package com.berti.testutils;

import com.berti.throttling.Throttler;
import com.berti.throttling.ThrottlerClient;
import lombok.Setter;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class ThrottlerMock implements Throttler {

    private final Set<ThrottlerClient> listeners = new CopyOnWriteArraySet<>();

    @Setter
    private boolean shouldProceed = false;

    public void sendNotification() {
        for (ThrottlerClient listener : listeners) {
            listener.proceedThrottledEvent();
        }
    }

    @Override
    public ThrottleResult shouldProceed() {
        return shouldProceed? ThrottleResult.PROCEED : ThrottleResult.DO_NOT_PROCEED;
    }

    @Override
    public void notifyWhenCanProceed(ThrottlerClient throttlerClient) {
        listeners.add(throttlerClient);
    }
}
