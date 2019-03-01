package saros.whiteboard.gef.part;

import org.apache.batik.util.SVGConstants;
import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.swt.graphics.Color;
import saros.whiteboard.gef.model.SVGEllipseRecord;
import saros.whiteboard.gef.util.ColorUtils;

public class SVGEllipsePart extends ElementRecordPart {

  @Override
  protected IFigure createFigure() {

    // TODO provide custom Ellipse with elliptical border
    IFigure figure = new Ellipse();

    XYLayout layout = new XYLayout();
    figure.setLayoutManager(layout);
    figure.setBackgroundColor(new Color(null, ColorUtils.getRGBBackgroundColor()));

    try {
      SVGEllipseRecord r = (SVGEllipseRecord) getElementRecord();
      String s = r.getAttributeValue(SVGConstants.SVG_FILL_ATTRIBUTE);
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
        figure.setForegroundColor(
            new Color(
                null,
                Integer.parseInt(rbg[0]),
                Integer.parseInt(rbg[1]),
                Integer.parseInt(rbg[2])));
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
    return figure;
  }
}
