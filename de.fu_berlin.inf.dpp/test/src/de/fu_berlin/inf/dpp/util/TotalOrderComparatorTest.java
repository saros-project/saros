package de.fu_berlin.inf.dpp.util;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.Test;

/**
 * 
 * @author Lindner, Andreas und Marcus
 * 
 */

public class TotalOrderComparatorTest {
    @Test
    public void testCompare() {
        List<String> randomStrings = new ArrayList<String>();
        
        // should be at least two strings
        for (int i = 0; i < 200; i++) {
            StringBuilder sb = new StringBuilder();
            Random rand = new Random();
    
            for (int j = 0; j < 1000; j++) {
                sb.append(rand.nextInt() % 10);
            }
            
            randomStrings.add(sb.toString());
        }
        
        TotalOrderComparator<String> comp = new TotalOrderComparator<String>();
        Collections.sort(randomStrings, comp);
        
        for (int i = 0; i < randomStrings.size() - 1; i++) {
            assertTrue(comp.compare(randomStrings.get(i), randomStrings.get(i + 1)) <= 0);
        }
    }
}
