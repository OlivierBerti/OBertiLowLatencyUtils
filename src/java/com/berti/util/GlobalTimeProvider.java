package com.berti.util;

// The purpose of the Global time classes is to be mocked in the unit tests
// Very useful when we need to mock time. e.g. in time windowed statistics

public class GlobalTimeProvider {

    private static final GlobalTime DEFAULT_IMPL = new GlobalTimeImpl();

    private static volatile GlobalTime impl = null;

    private static final Object lock = new Object();

    public static void setGlobalTimeSpecialImpl(GlobalTime specialImpl) {
        synchronized (lock) {
            if (impl == null) {
                impl = specialImpl;
            }
        }
    }

    public static GlobalTime getGlobalTime() {
        if (impl == null) {
            synchronized (lock) {
                if (impl == null) {
                    impl = DEFAULT_IMPL;
                }
            }
        }
        return impl;
    }
}
