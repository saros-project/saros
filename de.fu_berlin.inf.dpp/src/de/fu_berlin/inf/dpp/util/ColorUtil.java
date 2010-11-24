package de.fu_berlin.inf.dpp.util;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

public class ColorUtil {
    private static Logger log = Logger.getLogger(ColorUtil.class);

    /**
     * Scales a color; the higher the ratio the lighter the color
     * 
     * @param rgb
     *            that defines the color
     * @param scale
     *            ranges from 0 to 1 whereas 1 results in white and 0 in black
     * @return the scaled RGB
     */
    public static RGB scaleColor(RGB rgb, double scale) {
        if (scale < 0 || scale > 1)
            log.error("Invalid range; scaling ratio must be between 0 and 1.");
        int red = (int) ((1.0 - scale) * rgb.red + 255 * scale);
        int green = (int) ((1.0 - scale) * rgb.green + 255 * scale);
        int blue = (int) ((1.0 - scale) * rgb.blue + 255 * scale);
        return new RGB(red, green, blue);
    }

    /**
     * Scales a color; the higher the ratio the lighter the color
     * 
     * @param color
     *            to be scaled
     * @param scale
     *            ranges from 0 to 1 whereas 1 results in white and 0 in black
     * @return the scaled color
     *         <p>
     *         Note: Please keep in mind that a <strong>new</strong> color is
     *         created. Both the already existing and the scaled need to be
     *         disposed!
     */
    public static Color scaleColor(Color color, double scale) {
        RGB scaledRGB = scaleColor(color.getRGB(), scale);
        Color newColor = new Color(color.getDevice(), scaledRGB);
        return newColor;
    }

}
