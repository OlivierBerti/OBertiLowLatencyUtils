package com.berti.data;

import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Stream;


// A list destined to be used in a ring buffer
public class DataArrayList<T> {

    private T[] array;

    private final Supplier<T> supplier;

    private final Class<T> objectType;

    private final DataSetter<T> dataSetter;

    private int usedSize;

    @SuppressWarnings("unchecked")
    public DataArrayList(int capacity, Supplier<T> supplier, DataSetter<T> dataSetter) {
        this.supplier = supplier;
        this.dataSetter = dataSetter;
        this.objectType = (Class<T>) supplier.get().getClass();
        this.array = (T[]) Array.newInstance(objectType, Math.max(capacity, 1));
        for (int i = 0; i < array.length; i++) {
            array[i] = supplier.get();
        }
        this.usedSize = 0;
    }

    public void clear() {
        usedSize = 0;
    }

    public void addCopy(T element) {
        if (usedSize == array.length) {
            doubleCapacity();
        }
        dataSetter.copyData(element, array[usedSize]);
        usedSize++;
    }

    private void doubleCapacity() {
        T[] newArray = (T[]) Array.newInstance(objectType, array.length * 2);
        System.arraycopy(array, 0, newArray, 0, this.usedSize);
        for (int i = this.usedSize; i < newArray.length; i++) {
            newArray[i] = supplier.get();
        }
        array = newArray;
    }

    public T get(int index) {
        if (index < 0 || index >= usedSize) {
            throw new IndexOutOfBoundsException();
        }
        return array[index];
    }

    public void set(DataArrayList<T> source) {
        clear();
        while (this.array.length < source.size()) {
            doubleCapacity();
        }
        for (int i = 0; i < source.size(); i++) {
            addCopy(source.get(i));
        }
    }

    public int size() {
        return usedSize;
    }

    public Stream<T> stream() {
        return Arrays.stream(array).limit(usedSize);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getRealArray());
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof DataArrayList)) {
            return false;
        }
        DataArrayList<?> that = (DataArrayList<?>) obj;
        if (!that.objectType.equals(this.objectType)) {
            return false;
        }
        return Arrays.equals(this.getRealArray(), that.getRealArray());
    }

    private T[] getRealArray() {
        return ArrayUtils.subarray(array,0, usedSize);
    }

}
