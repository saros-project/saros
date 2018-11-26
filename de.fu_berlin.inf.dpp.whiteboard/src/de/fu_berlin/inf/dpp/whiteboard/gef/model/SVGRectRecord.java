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

public class SVGRectRecord extends LayoutElementRecord {

  public SVGRectRecord(DocumentRecord documentRecord) {
    super(documentRecord);
    setName(SVGConstants.SVG_RECT_TAG);
  }

  @Override
  public List<IRecord> createLayoutRecords(Rectangle layout, boolean onlyCreateNewRecords) {
    List<IRecord> records = new ArrayList<IRecord>(4);

    records.add(
        createNewOrSetAttributeRecord(
            null, SVGConstants.SVG_X_ATTRIBUTE, String.valueOf(layout.x), onlyCreateNewRecords));
    records.add(
        createNewOrSetAttributeRecord(
            null, SVGConstants.SVG_Y_ATTRIBUTE, String.valueOf(layout.y), onlyCreateNewRecords));
    records.add(
        createNewOrSetAttributeRecord(
            null,
            SVGConstants.SVG_WIDTH_ATTRIBUTE,
            String.valueOf(layout.width),
            onlyCreateNewRecords));
    records.add(
        createNewOrSetAttributeRecord(
            null,
            SVGConstants.SVG_HEIGHT_ATTRIBUTE,
            String.valueOf(layout.height),
            onlyCreateNewRecords));
    records.add(
        createNewOrSetAttributeRecord(
            null,
            SVGConstants.SVG_FILL_ATTRIBUTE,
            String.valueOf(ColorUtils.getBackgroundColor()),
            onlyCreateNewRecords));
    records.add(
        createNewOrSetAttributeRecord(
            null,
            SVGConstants.SVG_COLOR_ATTRIBUTE,
            String.valueOf(ColorUtils.getForegroundColor()),
            onlyCreateNewRecords));
    return records;
  }

  @Override
  public Point getLocation() {
    try {
      int x = getAttributeInt(SVGConstants.SVG_X_ATTRIBUTE);
      int y = getAttributeInt(SVGConstants.SVG_Y_ATTRIBUTE);
      return new Point(x, y);
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public Dimension getSize() {
    try {
      int width = getAttributeInt(SVGConstants.SVG_WIDTH_ATTRIBUTE);
      int height = getAttributeInt(SVGConstants.SVG_HEIGHT_ATTRIBUTE);
      return new Dimension(width, height);
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public boolean isComposite() {
    return true;
  }
}
