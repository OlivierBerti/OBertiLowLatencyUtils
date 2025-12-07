package com.berti.util;

import java.time.Instant;

public class GlobalTimeImpl implements GlobalTime {

    @Override
    public long getTimeMs() {
        return Instant.now().toEpochMilli();
    }
}
