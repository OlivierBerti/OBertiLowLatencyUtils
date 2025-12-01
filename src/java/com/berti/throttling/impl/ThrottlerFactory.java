package com.berti.throttling.impl;

import com.berti.eventbus.multithread.ringbuffer.RingBufferException;
import com.berti.throttling.Throttler;
import com.berti.throttling.ThrottlingConfiguration;
import com.berti.throttling.ThrottlingException;

public class ThrottlerFactory {

    private static final ThrottlerFactory instance = new ThrottlerFactory();

    public static ThrottlerFactory getInstance() {
        return instance;
    }

    public Throttler createThrottler(ThrottlingConfiguration throttlingConfiguration) throws ThrottlingException {
        try {
            ThrottlerImpl throtlerImpl = new ThrottlerImpl(throttlingConfiguration);
            throtlerImpl.start();
            return throtlerImpl;
        } catch (RingBufferException e) {
            throw new ThrottlingException("Throttler creation error: " + e.getMessage(), e);
        }
    }
}
