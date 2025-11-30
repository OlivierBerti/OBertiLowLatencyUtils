package com.berti.data;

public interface DataSetter<T> {

    void copyData(T source, T target);
}
