package de.fu_berlin.inf.dpp.ui.util;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.TextStyle;

/** This is used to format a part of a StyledString in BOLD letters (useful for treeviews) */
public class SWTBoldStyler extends Styler {

  public static final SWTBoldStyler STYLER = new SWTBoldStyler();

  private SWTBoldStyler() {
    super();
    int defHeight = (int) JFaceResources.getFontRegistry().defaultFont().getFontData()[0].height;
    // Create the font data so that it becomes bold.
    FontData fontData = new FontData(SWTBoldStyler.class.getSimpleName(), defHeight, SWT.BOLD);
    // Put the font in the registry.
    JFaceResources.getFontRegistry()
        .put(SWTBoldStyler.class.getSimpleName(), new FontData[] {fontData});
  }

  @Override
  public void applyStyles(TextStyle textStyle) {
    textStyle.font = JFaceResources.getFontRegistry().get(SWTBoldStyler.class.getSimpleName());
  }
}
