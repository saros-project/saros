/**
 * 
 */
package de.fu_berlin.inf.dpp.util;

import static org.junit.Assert.assertTrue;

import org.eclipse.swt.graphics.RGB;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 */
public class ColorUtilsTest {

    private RGB colorBlack;
    private RGB colorWhite;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        colorBlack = new RGB(0, 0, 0);
        colorWhite = new RGB(255, 255, 255);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {

    }

    /**
     * Test method for
     * {@link de.fu_berlin.inf.dpp.util.ColorUtils#addLightness(org.eclipse.swt.graphics.RGB, float)}
     * .
     */
    @Test
    public void testAddLightnessRGBFloat() {
        RGB colorTest = new RGB(128, 128, 128);
        RGB shouldBeWhite = ColorUtils.addLightness(colorTest, +1);
        RGB shouldBeBlack = ColorUtils.addLightness(colorTest, -1);
        RGB shouldBeSame = ColorUtils.addLightness(colorTest, 0);

        assertTrue("should be White", shouldBeWhite.equals(colorWhite));
        assertTrue("should be Black", shouldBeBlack.equals(colorBlack));
        assertTrue("should be Same", shouldBeSame.equals(colorTest));
    }

    /**
     * Test method for
     * {@link de.fu_berlin.inf.dpp.util.ColorUtils#addLightness(org.eclipse.swt.graphics.RGB, float)}
     * .
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddLightnessRGBFloat2() {
        ColorUtils.addLightness(new RGB(128, 128, 128), -2);
    }

    /**
     * Test method for
     * {@link de.fu_berlin.inf.dpp.util.ColorUtils#addLightness(org.eclipse.swt.graphics.Color, float)}
     * .
     */
    @Test
    public void testAddLightnessColorFloat() {
        // TODO
        // how to get device?
        // its the same like RGB testing
    }

    @Test(expected = IllegalArgumentException.class)
    public void testScaleColorWithInvalidRange() {
        ColorUtils.scaleColorBy(new RGB(128, 128, 128), -1);
    }

    /**
     * Test method for
     * {@link de.fu_berlin.inf.dpp.util.ColorUtils#scaleColorBy(org.eclipse.swt.graphics.RGB, float)}
     * .
     */
    @Test
    public void testScaleColorByRGBFloat() {

        RGB colorTest = new RGB(128, 128, 128);
        RGB shouldBeBlack = ColorUtils.scaleColorBy(colorTest, 0);
        RGB shouldBeSame = ColorUtils.scaleColorBy(colorTest, 1);
        RGB shouldBeBrighter = ColorUtils.scaleColorBy(colorTest, 2);

        assertTrue("should be Black", shouldBeBlack.equals(colorBlack));
        assertTrue("should be the Same", shouldBeSame.equals(colorTest));
        assertTrue("should be brighter", shouldBeBrighter.red > 128);
    }

    /**
     * Test method for
     * {@link de.fu_berlin.inf.dpp.util.ColorUtils#scaleColorBy(org.eclipse.swt.graphics.Color, float)}
     * .
     */
    @Test
    public void testScaleColorByColorFloat() {
        // TODO
        // how to get device?
        // its the same like RGB testing
    }

}
