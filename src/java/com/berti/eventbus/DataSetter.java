package com.berti.eventbus;

public interface DataSetter<T> {

    void copyData(T source, T target);
}
