package de.fu_berlin.inf.dpp.whiteboard.gef.model;

import de.fu_berlin.inf.dpp.whiteboard.sxe.records.DocumentRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.ElementRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.IRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.SXEDefaultRecordFactory;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.serializable.RecordDataObject;
import de.fu_berlin.inf.dpp.whiteboard.sxe.util.SXEUtils;
import java.util.List;
import org.apache.batik.util.SVGConstants;
import org.apache.log4j.Logger;

/**
 * Custom implementation of the ISXERecordFactory to achieve specialized ElementRecord subclass
 * instances respective their tag name (i.e. SVGRectRecord for rect-tag).</br>
 *
 * <p>This class is also used to provide custom shape feedback for creation.
 *
 * @author jurke
 */
public class GEFRecordFactory extends SXEDefaultRecordFactory {

  Logger log = Logger.getLogger(GEFRecordFactory.class);

  @Override
  public ElementRecord createElementRecord(DocumentRecord documentRecord, String ns, String tag) {
    if (documentRecord != null) {
      List<IRecord> state = documentRecord.getState();
      List<RecordDataObject> list = SXEUtils.toDataObjects(state);
      log.debug("new Documentstate: " + list);
      // log.info(state.get(state.size() - 1).getRecordDataObject()
      // .getValuePairs().keySet());

    }
    ElementRecord r = null;
    if (tag.equals(SVGConstants.SVG_RECT_TAG)) {
      r = new SVGRectRecord(documentRecord);
    } else if (tag.equals(SVGConstants.SVG_POLYLINE_TAG)) {
      r = new SVGPolylineRecord(documentRecord);
    } else if (tag.equals(SVGConstants.SVG_ELLIPSE_TAG)) {
      r = new SVGEllipseRecord(documentRecord);
    } else if (tag.equals(SVGConstants.SVG_TEXT_TAG)) {
      r = new SVGTextBoxRecord(documentRecord);
    } else if (tag.equals(SVGConstants.SVG_SVG_TAG)) {
      r = new SVGRootRecord(documentRecord);
    } else {
      r = new ElementRecord(documentRecord);
    }
    return r;
  }

  @Override
  public SVGRootRecord createRoot(DocumentRecord documentRecord) {
    SVGRootRecord r = new SVGRootRecord(documentRecord);
    return r;
  }
}
