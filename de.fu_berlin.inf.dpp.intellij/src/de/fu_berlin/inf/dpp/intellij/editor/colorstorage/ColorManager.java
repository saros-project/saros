package de.fu_berlin.inf.dpp.intellij.editor.colorstorage;

import java.awt.Color;

/** IntelliJ color manager */
// todo: temporary implementation to provide default colors
public class ColorManager {

  public static final Color DEFAULT_COLOR = new Color(128, 128, 128);

  static final Color[] CONTRIBUTION_COLORS = {
    new Color(141, 206, 231),
    new Color(191, 187, 130),
    new Color(186, 220, 81),
    new Color(237, 237, 169),
    new Color(137, 180, 178)
  };

  static final Color[] SELECTION_COLORS = {
    new Color(183, 224, 240),
    new Color(208, 205, 164),
    new Color(220, 237, 166),
    new Color(246, 246, 211),
    new Color(184, 210, 209)
  };

  private ColorManager() {}

  /**
   * Returns the color for the given userID. Returns the DEFAULT_COLOR when there is no color for
   * the userID.
   *
   * @param userID
   * @return the color for the given userID.
   */
  public static ColorModel getColorModel(int userID) {

    if (userID < 0 || userID >= 5) {
      return new ColorModel(DEFAULT_COLOR, DEFAULT_COLOR);
    }

    return new ColorModel(CONTRIBUTION_COLORS[userID], SELECTION_COLORS[userID]);
  }
}
