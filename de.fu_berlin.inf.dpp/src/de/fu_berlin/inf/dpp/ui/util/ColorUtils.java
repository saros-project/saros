package de.fu_berlin.inf.dpp.ui.util;

import com.imagingbook.color.ColorSpaceConversion;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

public class ColorUtils {

  private ColorUtils() {
    // no instantiation allowed
  }

  /**
   * Increases a color's lightness
   *
   * @param rgb that defines the color
   * @param lightness by which the color's lightness should be increased;<br>
   *     range: -1 (results in black) to +1 (+1 results in white)
   * @return
   * @see <a href="http://en.wikipedia.org/wiki/HSL_and_HSV">HSL and HSV</a>
   */
  public static RGB addLightness(RGB rgb, float lightness) {
    if (lightness < -1 || lightness > 1) throw new IndexOutOfBoundsException();

    /*
     * Convert to HLS; HLS and HSL are synonym
     */
    float[] hls =
        ColorSpaceConversion.RGBtoHLS(
            (float) rgb.red / 255, (float) rgb.green / 255, (float) rgb.blue / 255);

    /*
     * Scale the lightness
     */
    hls[1] += lightness;
    if (hls[1] < 0) hls[1] = 0;
    if (hls[1] > 1) hls[1] = 1;

    /*
     * Convert back from HLS to RGB
     */
    float[] newRGB = ColorSpaceConversion.HLStoRGB(hls[0], hls[1], hls[2]);

    return new RGB(
        Math.round(newRGB[0] * 255), Math.round(newRGB[1] * 255), Math.round(newRGB[2] * 255));
  }

  /**
   * Increases a color's lightness
   *
   * @param color
   * @param lightness by which the color's lightness should be increased;<br>
   *     range: -1 (results in black) to +1 (+1 results in white)
   * @return the new color
   *     <p>Note: Please keep in mind that a <strong>new</strong> color is created. Both the already
   *     existing and the scaled need to be disposed after usage!
   * @see <a href="http://en.wikipedia.org/wiki/HSL_and_HSV">HSL and HSV</a>
   */
  public static Color addLightness(Color color, float lightness) {
    RGB newRGB = addLightness(color.getRGB(), lightness);
    Color newColor = new Color(color.getDevice(), newRGB);
    return newColor;
  }

  /**
   * Scales a color's lightness; the higher the ratio the lighter the color
   *
   * @param rgb that defines the color
   * @param scale non-negative whereas 0 results in zero lightness = black color
   * @return the scaled RGB
   * @see <a href="http://en.wikipedia.org/wiki/HSL_and_HSV">HSL and HSV</a>
   */
  public static RGB scaleColorBy(RGB rgb, float scale) {
    if (scale < 0) throw new IllegalArgumentException();

    /*
     * Convert to HLS; HLS and HSL are synonym
     */
    float[] hls =
        ColorSpaceConversion.RGBtoHLS(
            (float) rgb.red / 255, (float) rgb.green / 255, (float) rgb.blue / 255);

    /*
     * Scale the lightness
     */
    hls[1] *= scale;

    /*
     * Convert back from HLS to RGB
     */
    float[] scaledRGB = ColorSpaceConversion.HLStoRGB(hls[0], hls[1], hls[2]);

    return new RGB(
        Math.round(scaledRGB[0] * 255),
        Math.round(scaledRGB[1] * 255),
        Math.round(scaledRGB[2] * 255));
  }

  /**
   * Scales a color's lightness; the higher the ratio the lighter the color
   *
   * @param color to be scaled
   * @param scale non-negative whereas 0 results in zero lightness = black color
   * @return the scaled color
   *     <p>Note: Please keep in mind that a <strong>new</strong> color is created. Both the already
   *     existing and the scaled need to be disposed after usage!
   * @see <a href="http://en.wikipedia.org/wiki/HSL_and_HSV">HSL and HSV</a>
   */
  public static Color scaleColorBy(Color color, float scale) {
    RGB scaledRGB = scaleColorBy(color.getRGB(), scale);
    Color newColor = new Color(color.getDevice(), scaledRGB);
    return newColor;
  }
}
