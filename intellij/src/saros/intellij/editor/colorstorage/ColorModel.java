package de.fu_berlin.inf.dpp.intellij.editor.colorstorage;

import java.awt.Color;

/** Intellij color model */
public class ColorModel {
  private Color selectColor;
  private Color editColor;

  /**
   * Creates a ColorModel with editColor and selectColor.
   *
   * @param editColor
   * @param selectColor
   */
  public ColorModel(Color editColor, Color selectColor) {
    this.selectColor = selectColor;
    this.editColor = editColor;
  }

  public Color getSelectColor() {
    return selectColor;
  }

  public Color getEditColor() {
    return editColor;
  }
}
