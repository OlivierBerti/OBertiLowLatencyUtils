package com.berti.throttling;

public interface Throttler {
    // check if we can proceed (Poll)
    ThrottleResult shouldProceed();

    // subscribe to be told when we can proceed (Push)
    void  notifyWhenCanProceed(ThrottlerClient throttlerClient);

    enum ThrottleResult {
        PROCEED, // publish, aggregate etc
        DO_NOT_PROCEED //
    }
}
