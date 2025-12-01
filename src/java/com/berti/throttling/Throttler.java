package com.berti.throttling;

public interface Throttler {
    // check if we can proceed (poll)
    ThrottleResult shouldProceed();

    void  notifyWhenCanProceed(ThrottlerClient throttlerClient);

    enum ThrottleResult {
        PROCEED, // publish, aggregate etc
        DO_NOT_PROCEED //
    }
}
