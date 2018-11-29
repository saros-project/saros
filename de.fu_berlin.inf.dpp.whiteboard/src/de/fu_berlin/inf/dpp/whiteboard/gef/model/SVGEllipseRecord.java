package de.fu_berlin.inf.dpp.whiteboard.gef.model;

import de.fu_berlin.inf.dpp.whiteboard.gef.util.ColorUtils;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.DocumentRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.IRecord;
import java.util.ArrayList;
import java.util.List;
import org.apache.batik.util.SVGConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

public class SVGEllipseRecord extends LayoutElementRecord {

  public SVGEllipseRecord(DocumentRecord documentRecord) {
    super(documentRecord);
    setName(SVGConstants.SVG_ELLIPSE_TAG);
  }

  @Override
  public Point getLocation() {
    int x =
        getAttributeInt(SVGConstants.SVG_CX_ATTRIBUTE)
            - getAttributeInt(SVGConstants.SVG_RX_ATTRIBUTE);
    int y =
        getAttributeInt(SVGConstants.SVG_CY_ATTRIBUTE)
            - getAttributeInt(SVGConstants.SVG_RY_ATTRIBUTE);
    return new Point(x, y);
  }

  @Override
  public Dimension getSize() {
    int width = 2 * getAttributeInt(SVGConstants.SVG_RX_ATTRIBUTE);
    int height = 2 * getAttributeInt(SVGConstants.SVG_RY_ATTRIBUTE);
    return new Dimension(width, height);
  }

  @Override
  public List<IRecord> createLayoutRecords(Rectangle layout, boolean withoutSetRecords) {
    List<IRecord> records = new ArrayList<IRecord>(4);

    records.add(
        createNewOrSetAttributeRecord(
            null,
            SVGConstants.SVG_CX_ATTRIBUTE,
            String.valueOf(layout.x + layout.width / 2),
            withoutSetRecords));
    records.add(
        createNewOrSetAttributeRecord(
            null,
            SVGConstants.SVG_CY_ATTRIBUTE,
            String.valueOf(layout.y + layout.height / 2),
            withoutSetRecords));
    records.add(
        createNewOrSetAttributeRecord(
            null,
            SVGConstants.SVG_RX_ATTRIBUTE,
            String.valueOf(layout.width / 2),
            withoutSetRecords));
    records.add(
        createNewOrSetAttributeRecord(
            null,
            SVGConstants.SVG_RY_ATTRIBUTE,
            String.valueOf(layout.height / 2),
            withoutSetRecords));
    records.add(
        createNewOrSetAttributeRecord(
            null,
            SVGConstants.SVG_FILL_ATTRIBUTE,
            String.valueOf(ColorUtils.getBackgroundColor()),
            withoutSetRecords));
    records.add(
        createNewOrSetAttributeRecord(
            null,
            SVGConstants.SVG_COLOR_ATTRIBUTE,
            String.valueOf(ColorUtils.getForegroundColor()),
            withoutSetRecords));
    return records;
  }

  @Override
  public boolean isComposite() {
    return true;
  }
}
