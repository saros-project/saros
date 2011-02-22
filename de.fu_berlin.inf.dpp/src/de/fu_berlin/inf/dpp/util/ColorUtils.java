package de.fu_berlin.inf.dpp.util;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import com.imagingbook.color.ColorSpaceConversion;

public class ColorUtils {
    private static Logger log = Logger.getLogger(ColorUtils.class);

    private ColorUtils() {
        // no instantiation allowed
    }

    /**
     * Scales a color's lightness; the higher the ratio the lighter the color
     * 
     * @param rgb
     *            that defines the color
     * @param scale
     *            non-negative whereas 0 results in zero lightness = black color
     * @return the scaled RGB
     * 
     * @see <a href="http://en.wikipedia.org/wiki/HSL_and_HSV">HSL and HSV</a>
     */
    public static RGB scaleColorBy(RGB rgb, float scale) {
        if (scale < 0)
            log.error("Invalid range; scale must be non-negative");

        /*
         * Convert to HLS; HLS and HSL are synonym
         */
        float[] hls = ColorSpaceConversion.RGBtoHLS((float) rgb.red / 255,
            (float) rgb.green / 255, (float) rgb.blue / 255);

        /*
         * Scale the lightness
         */
        hls[1] *= scale;

        /*
         * Convert back from HLS to RGB
         */
        float[] scaledRGB = ColorSpaceConversion.HLStoRGB(hls[0], hls[1],
            hls[2]);

        return new RGB(Math.round(scaledRGB[0] * 255),
            Math.round(scaledRGB[1] * 255), Math.round(scaledRGB[2] * 255));
    }

    /**
     * Scales a color's lightness; the higher the ratio the lighter the color
     * 
     * @param color
     *            to be scaled
     * @param scale
     *            non-negative whereas 0 results in zero lightness = black color
     * @return the scaled color
     *         <p>
     *         Note: Please keep in mind that a <strong>new</strong> color is
     *         created. Both the already existing and the scaled need to be
     *         disposed after usage!
     * 
     * @see <a href="http://en.wikipedia.org/wiki/HSL_and_HSV">HSL and HSV</a>
     */
    public static Color scaleColorBy(Color color, float scale) {
        RGB scaledRGB = scaleColorBy(color.getRGB(), scale);
        Color newColor = new Color(color.getDevice(), scaledRGB);
        return newColor;
    }

}
