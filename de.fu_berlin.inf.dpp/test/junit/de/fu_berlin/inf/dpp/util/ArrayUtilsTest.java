package de.fu_berlin.inf.dpp.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ArrayUtilsTest {

    @Test
    public void testGetInstances() {

        List<Object> objects = new ArrayList<Object>();

        objects.add(new Integer(1));
        objects.add(new Integer(5));
        objects.add(new Short((short) 1));
        objects.add(new Float(1));
        objects.add(new Double(1));

        List<Number> numbers = ArrayUtils.getInstances(objects.toArray(),
            Number.class);

        assertEquals(objects.size(), numbers.size());

        List<Integer> integers = ArrayUtils.getInstances(objects.toArray(),
            Integer.class);

        assertEquals(2, integers.size());
    }
}
