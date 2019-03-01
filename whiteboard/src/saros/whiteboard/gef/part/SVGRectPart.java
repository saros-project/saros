package de.fu_berlin.inf.dpp.whiteboard.gef.part;

import de.fu_berlin.inf.dpp.whiteboard.gef.model.SVGRectRecord;
import de.fu_berlin.inf.dpp.whiteboard.gef.util.ColorUtils;
import org.apache.batik.util.SVGConstants;
import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.swt.graphics.Color;

public class SVGRectPart extends ElementRecordPart {

  @Override
  protected IFigure createFigure() {
    // ISarosSession session = WhiteboardManager.getInstance()
    // .getSarosSession();
    // User user = session.getLocalUser();

    IFigure figure = new RectangleFigure();
    org.eclipse.draw2d.XYLayout layout = new XYLayout();
    figure.setLayoutManager(layout);

    figure.setBackgroundColor(new Color(null, ColorUtils.getRGBBackgroundColor()));

    Border line = new LineBorder(new Color(null, ColorUtils.getRGBForegroundColor()));
    figure.setBorder(line);
    try {
      SVGRectRecord r = (SVGRectRecord) getElementRecord();
      String s = r.getAttributeValue(SVGConstants.SVG_FILL_ATTRIBUTE);
      System.out.println("STRING S : " + s);
      if (s != null) {
        String rbg[] = s.split(",");
        figure.setBackgroundColor(
            new Color(
                null,
                Integer.parseInt(rbg[0]),
                Integer.parseInt(rbg[1]),
                Integer.parseInt(rbg[2])));
      }
      String s2 = r.getAttributeValue(SVGConstants.SVG_COLOR_ATTRIBUTE);
      if (s2 != null) {
        String rbg[] = s2.split(",");
        figure.setBorder(
            new LineBorder(
                new Color(
                    null,
                    Integer.parseInt(rbg[0]),
                    Integer.parseInt(rbg[1]),
                    Integer.parseInt(rbg[2]))));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return figure;
  }
}
