package com.berti.testutils;

import com.berti.util.GlobalTime;

//Mocking the time
public class TimeMsMock implements GlobalTime {

    private long timeMs = 0L;

    public void reset() {
        timeMs = 0L;
    }

    public void sleep(long sleepingTime) {
        timeMs += sleepingTime;
    }

    @Override
    public long getTimeMs() {
        return timeMs;
    }
}
