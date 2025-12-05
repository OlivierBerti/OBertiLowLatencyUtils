package com.berti.ringbuffer;

import com.berti.data.DataSetter;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public final class DataSetterRegistry {

    private static final Map<Class<?>, DataSetter> dataSetters = new HashMap<>();

    public static <T> void register(Class<T> clazz, DataSetter<T> dataSetter) {
        dataSetters.put(clazz, dataSetter);
    }

    @SuppressWarnings("unchecked")
    public static <T> DataSetter<T> getDataSetter(Class<T> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("clazz cannot be null");
        }
        DataSetter<T> dataSetter = dataSetters.get(clazz);
        if (dataSetter == null) {
            throw new NoSuchElementException("No DataSetter registered for " + clazz);
        }
        return dataSetter;
    }

    private DataSetterRegistry() {}
}
