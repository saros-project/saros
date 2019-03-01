package saros.whiteboard.gef.part;

import org.apache.batik.util.SVGConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Polyline;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.swt.graphics.Color;
import saros.whiteboard.gef.model.SVGPolylineRecord;
import saros.whiteboard.gef.util.ColorUtils;

public class SVGPolylinePart extends ElementRecordPart {

  @Override
  protected IFigure createFigure() {
    Polyline figure = new Polyline();

    SVGPolylineRecord record = (SVGPolylineRecord) getElementRecord();
    figure.setPoints(record.getPoints().getCopy());

    XYLayout layout = new XYLayout();
    figure.setLayoutManager(layout);
    figure.setForegroundColor(new Color(null, ColorUtils.getRGBForegroundColor()));
    // figure.setForegroundColor(new Color(null, 0, 0, 0));

    try {
      SVGPolylineRecord r = (SVGPolylineRecord) getElementRecord();
      String s = r.getAttributeValue(SVGConstants.SVG_COLOR_ATTRIBUTE);
      if (s != null) {
        String rbg[] = s.split(",");
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

  @Override
  protected void refreshVisuals() {
    Polyline line = (Polyline) getFigure();
    SVGPolylineRecord record = (SVGPolylineRecord) getElementRecord();
    line.setPoints(record.getPoints());

    super.refreshVisuals();

    // hack because selection handle does not update
    if (getViewer().getSelectedEditParts().contains(this)
        && getElementRecord().isPartOfVisibleDocument()) getViewer().appendSelection(this);
  }
}
