package de.fu_berlin.inf.dpp.whiteboard.gef.model;

import org.apache.batik.util.SVGConstants;

import de.fu_berlin.inf.dpp.whiteboard.sxe.records.DocumentRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.ElementRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.SXEDefaultRecordFactory;

/**
 * Custom implementation of the ISXERecordFactory to achieve specialized
 * ElementRecord subclass instances respective their tag name (i.e.
 * SVGRectRecord for rect-tag).</br>
 * 
 * This class is also used to provide custom shape feedback for creation.
 * 
 * @author jurke
 * 
 */
public class GEFRecordFactory extends SXEDefaultRecordFactory {

	@Override
	public ElementRecord createElementRecord(DocumentRecord documentRecord,
			String ns, String tag) {

		ElementRecord r = null;
		if (tag.equals(SVGConstants.SVG_RECT_TAG)) {
			r = new SVGRectRecord(documentRecord);
		} else if (tag.equals(SVGConstants.SVG_POLYLINE_TAG)) {
			r = new SVGPolylineRecord(documentRecord);
		} else if (tag.equals(SVGConstants.SVG_ELLIPSE_TAG)) {
			r = new SVGEllipseRecord(documentRecord);
		} else if (tag.equals(SVGConstants.SVG_SVG_TAG)) {
			r = new SVGRootRecord(documentRecord);
		}
		return r;

	}

	@Override
	public SVGRootRecord createRoot(DocumentRecord documentRecord) {
		SVGRootRecord r = new SVGRootRecord(documentRecord);
		return r;
	}

}
