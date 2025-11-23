package com.berti.eventbus;

public interface EventBusSubscriber<T> {

    void onEvent(T event);
}
