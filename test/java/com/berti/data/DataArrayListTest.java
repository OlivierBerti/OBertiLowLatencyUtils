package com.berti.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.junit.Before;
import org.junit.Test;

import java.util.Comparator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class DataArrayListTest {

    @Getter
    @Setter
    @EqualsAndHashCode
    private static final class Sample {
        private String name;
        private int value;
    }

    private static final class SampleDataSetter implements DataSetter<Sample> {

        @Override
        public void copyData(Sample source, Sample target) {
            target.name = source.name;
            target.value = source.value;
        }
    }

    private DataSetter<Sample> sampleDataSetter;

    private Sample sample1;
    private Sample sample2;
    private Sample sample3;
    private Sample sample4;

    @Before
    public void setUp() {
        sampleDataSetter = new SampleDataSetter();

        sample1 = new Sample();
        sample1.setName("A");
        sample1.setValue(100);

        sample2 = new Sample();
        sample2.setName("B");
        sample2.setValue(200);

        sample3 = new Sample();
        sample3.setName("C");
        sample3.setValue(20);

        sample4 = new Sample();
        sample4.setName("D");
        sample4.setValue(1);
    }

    @Test
    public void testGetAndSet() {
        DataArrayList<Sample> samples = new DataArrayList<>(10, Sample::new, sampleDataSetter);

        assertEquals(0, samples.size());
        samples.addCopy(sample1);
        assertEquals(1, samples.size());
        samples.addCopy(sample2);
        assertEquals(2, samples.size());
        assertEquals(sample1, samples.get(0));
        assertEquals(sample2, samples.get(1));
    }

    @Test
    public void testEquals() {
        DataArrayList<Sample> list1 = new DataArrayList<>(10, Sample::new, sampleDataSetter);

        // With initial capacity at 1 the double capacity mechanism will be called
        DataArrayList<Sample> list2 = new DataArrayList<>(1, Sample::new, sampleDataSetter);

        list1.addCopy(sample1);
        list1.addCopy(sample2);
        list2.addCopy(sample1);
        list2.addCopy(sample2);
        assertEquals(list1, list2);

        list2.clear();
        assertNotEquals(list1, list2);

        list2.addCopy(sample3);
        list2.addCopy(sample1);
        list2.addCopy(sample4);
        list2.addCopy(sample3);
        assertNotEquals(list1, list2);

        list2.clear();
        list2.addCopy(sample1);
        list2.addCopy(sample2);
        assertEquals(list1, list2);
    }

    @Test
    public void testStream() {
        DataArrayList<Sample> list = new DataArrayList<>(10, Sample::new, sampleDataSetter);

        // We make sample3 and sample4 in the raw array to test they are correctly ignored
        list.addCopy(sample3);
        list.addCopy(sample1);
        list.addCopy(sample4);
        list.addCopy(sample3);

        list.clear();
        list.addCopy(sample2);
        list.addCopy(sample1);

        Sample[] res =  list.stream()
                        .sorted(Comparator.comparingInt(Sample::getValue))
                        .toArray(Sample[]::new);
        Sample[] expected = new Sample[]{sample1, sample2};
        assertEquals(expected, res);
    }
}
