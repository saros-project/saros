package de.fu_berlin.inf.dpp.whiteboard.gef.part;

import de.fu_berlin.inf.dpp.whiteboard.gef.model.SVGTextBoxRecord;
import org.apache.batik.util.SVGConstants;
import org.apache.log4j.Logger;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

/**
 * this class acts as Listener on changes in the records, it calls different methods on creation and
 * editing of the figure
 *
 * @author Markus
 */
public class SVGTextBoxPart extends ElementRecordPart {
  private static final int TEXT_SIZE = 16;
  private static final String TEXT_TYPE = "Arial";

  private static final Logger log = Logger.getLogger(SVGTextBoxPart.class);

  @Override
  protected IFigure createFigure() {
    Label figure = new Label();
    SVGTextBoxRecord r = (SVGTextBoxRecord) getElementRecord();
    String s = null;

    if (r != null) s = r.getAttributeValue(SVGConstants.SVG_TEXT_VALUE);

    if (s == null || s.isEmpty()) s = "New Text";

    figure.setText(s);

    log.trace("Create TextBox Figure - Text: " + s);

    XYLayout layout = new XYLayout();
    figure.setLayoutManager(layout);
    figure.setForegroundColor(ColorConstants.black);

    try {
      String s2 = r.getAttributeValue(SVGConstants.SVG_COLOR_ATTRIBUTE);
      if (s2 != null) {
        String rbg[] = s2.split(",");
        figure.setForegroundColor(
            new Color(
                null,
                Integer.parseInt(rbg[0]),
                Integer.parseInt(rbg[1]),
                Integer.parseInt(rbg[2])));
      }
    } catch (Exception e) {
      log.error(e.getMessage());
    }

    return figure;
  }

  /**
   * Refreshes the Text displayed in the figure with the text saved in the model
   *
   * @param text
   */
  protected void refreshText() {
    SVGTextBoxRecord r = (SVGTextBoxRecord) getElementRecord();
    Label figure = (Label) getFigure();
    if (r != null) {

      // Label needs a huge text-size so it can be displayed
      // Size of text depends on current zoom level
      Font classFont = new Font(null, TEXT_TYPE, (int) (TEXT_SIZE / getCurrentZoom()), SWT.NORMAL);
      figure.setFont(classFont);

      Object s = r.getAttributeValue(SVGConstants.SVG_TEXT_VALUE);
      String oldtext = ((Label) getFigure()).getText();

      /*
       * If there has been already a text, but the model does not contain
       * it (happens when peer received the figure, it does not has a text
       * in the model, so set it now) [FIXME] model should contain text on
       * creation, not with this hack see: createChangeConstraintCommand
       * in ElementModelLayoutEditPolicy
       */
      log.trace(
          "refresh text: old:"
              + oldtext
              + " - "
              + (oldtext == "null")
              + ","
              + (oldtext == null)
              + " new:"
              + s
              + " - "
              + (s == "null")
              + ","
              + (s == null));
      // null does not seem to work somehow
      s = oldtext;
      r.setText(oldtext);
      figure.setText(oldtext);

      log.info("Refreshed Text: " + s);
    }
  }

  @Override
  protected void refreshVisuals() {
    refreshText();
    super.refreshVisuals();
  }
}
