package de.fu_berlin.inf.dpp.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

public class MappingIteratorTest {

    @Test
    public void testIterator() {

        List<Integer> integers = new ArrayList<Integer>();

        Function<Integer, Integer> f = new Function<Integer, Integer>() {
            public Integer apply(Integer u) {
                return 2 << u;
            }
        };

        for (int i = 1; i < 16; i++)
            integers.add(i);

        Iterator<Integer> it = integers.iterator();

        MappingIterator<Integer, Integer> mit = new MappingIterator<Integer, Integer>(
            it, f);

        int i = 1;
        while (mit.hasNext()) {
            assertEquals(2 << i++, mit.next().intValue());
        }
    }
}