package de.fu_berlin.inf.dpp.intellij.editor.colorstorage;

import com.intellij.openapi.editor.markup.RangeHighlighter;

import java.awt.Color;

/**
 * Intellij color model
 */
public class ColorModel {
    private Color selectColor;
    private Color editColor;
    private RangeHighlighter select;

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

    public RangeHighlighter getSelect() {
        return select;
    }

    public void setSelect(RangeHighlighter select) {
        this.select = select;
    }
}
