package saros.whiteboard.gef.model;

import java.util.ArrayList;
import java.util.List;
import org.apache.batik.util.SVGConstants;
import org.apache.log4j.Logger;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import saros.whiteboard.gef.util.ColorUtils;
import saros.whiteboard.sxe.records.DocumentRecord;
import saros.whiteboard.sxe.records.IRecord;

public class SVGTextBoxRecord extends LayoutElementRecord {

  Logger log = Logger.getLogger(SVGTextBoxRecord.class);
  private String text;

  public String getText() {
    return text;
  }

  public void setText(String t) {
    text = t;
    log.trace("Set Text:" + t);
  }

  public SVGTextBoxRecord(DocumentRecord documentRecord) {
    super(documentRecord);
    setName(SVGConstants.SVG_TEXT_TAG);

    log.trace("Created SVGTextBoxRecord");
  }

  @Override
  public List<IRecord> createLayoutRecords(Rectangle layout, boolean onlyCreateNewRecords) {
    List<IRecord> records = new ArrayList<IRecord>();
    // log.debug("Rect: " + layout);
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
            SVGConstants.SVG_TEXT_VALUE,
            String.valueOf(this.getText()),
            onlyCreateNewRecords));
    records.add(
        createNewOrSetAttributeRecord(
            null,
            SVGConstants.SVG_COLOR_ATTRIBUTE,
            String.valueOf(ColorUtils.getForegroundColor()),
            onlyCreateNewRecords));
    log.trace(
        "create Layout Records: Text:"
            + this.text
            + " X:"
            + layout.x
            + " Y:"
            + layout.y
            + " Width:"
            + layout.width
            + " Height:"
            + layout.height);
    return records;
  }

  @Override
  public Point getLocation() {
    try {
      int x = getAttributeInt(SVGConstants.SVG_X_ATTRIBUTE);
      int y = getAttributeInt(SVGConstants.SVG_Y_ATTRIBUTE);
      log.trace("Location: x:" + x + " y:" + y);
      return new Point(x, y);
    } catch (NullPointerException e) {
      log.error("Unexpected null pointer exception: " + e.getMessage());
    } catch (NumberFormatException e) {
      log.error("Bad format in object's attributes: " + e.getMessage());
    }

    return null;
  }

  @Override
  public Dimension getSize() {
    try {
      int width = getAttributeInt(SVGConstants.SVG_WIDTH_ATTRIBUTE);
      int height = getAttributeInt(SVGConstants.SVG_HEIGHT_ATTRIBUTE);
      log.trace("Size: width:" + width + " height:" + height);
      return new Dimension(width, height);
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public boolean isComposite() {
    return false;
  }
}
