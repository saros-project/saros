/**
 * 
 */
package de.fu_berlin.inf.dpp.project.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * 
 */
public class FreeColorsTest {

    /**
     * Test method for
     * {@link de.fu_berlin.inf.dpp.project.internal.FreeColors#FreeColors(int)}.
     */
    @Test
    public void testFreeColors() {
        int number = 10;
        FreeColors fc = new FreeColors(number);
        assertEquals("The list should contain " + number
            + " elements, but contains " + fc.freeColors.size() + " elements.",
            number, fc.freeColors.size());
    }

    /**
     * Test method for
     * {@link de.fu_berlin.inf.dpp.project.internal.FreeColors#get()}.
     */
    @Test
    public void testGet() {
        int number = 10;
        FreeColors fc = new FreeColors(number);
        assertFalse("The returned color shouldn't be maxColorID.",
            fc.get() == number);

    }

    /**
     * Test method for
     * {@link de.fu_berlin.inf.dpp.project.internal.FreeColors#add(int)}.
     * 
     * everything is true
     */
    @Test
    public void testAdd() {
        int number = 10;
        FreeColors fc = new FreeColors(number);
        int freeColor = fc.get();
        fc.add(freeColor);
        assertTrue("The list should conatain the color.",
            fc.freeColors.contains(freeColor));
    }

    /**
     * Test method for
     * {@link de.fu_berlin.inf.dpp.project.internal.FreeColors#add(int)}.
     * 
     * newColor>maxID
     */
    @Test
    public void testAdd3() {
        int number = 10;
        FreeColors fc = new FreeColors(number);
        int freeColor = 12;
        fc.add(freeColor);
        assertFalse("The list shouldn't conatain the color.",
            fc.freeColors.contains(freeColor));
    }
}
