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
        FreeColors fc2 = new FreeColors(1);
        assertEquals("The list should be empty because maxColorID<=1", 0,
            fc2.freeColors.size());
        int number = 10;
        FreeColors fc = new FreeColors(number);
        assertEquals("The list should contain" + (number - 1)
            + " elements, but contains " + fc.freeColors.size() + " elements.",
            number - 1, fc.freeColors.size());
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
     * {@link de.fu_berlin.inf.dpp.project.internal.FreeColors#get()}.
     * 
     * Tests the try/catch block.
     */
    @Test
    public void testGet2() {
        int number = 1;
        FreeColors fc = new FreeColors(number);
        assertTrue("The returned color should be maxColorID.",
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
     * id<=0
     */
    @Test
    public void testAdd2() {
        int number = 10;
        FreeColors fc = new FreeColors(number);
        fc.add(0);
        assertFalse("The list shouldn't conatain the color.",
            fc.freeColors.contains(0));
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
