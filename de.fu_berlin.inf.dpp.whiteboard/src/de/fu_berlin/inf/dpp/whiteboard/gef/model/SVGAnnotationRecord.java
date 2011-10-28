package de.fu_berlin.inf.dpp.whiteboard.gef.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.batik.util.SVGConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;

import de.fu_berlin.inf.dpp.whiteboard.sxe.records.DocumentRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.IRecord;

public class SVGAnnotationRecord extends SVGTextBoxRecord {

	public SVGAnnotationRecord(DocumentRecord documentRecord) {
		super(documentRecord);
		setName(SVGConstants.SVG_ANNOTATION_TAG);
		log.info("Created SVGAnnotationRecord");
	}

	@Override
	public boolean isComposite() {
		return false;
	}

	@Override
	public List<IRecord> createLayoutRecords(Rectangle layout,
			boolean onlyCreateNewRecords) {

		int a = Math.min(layout.width, layout.height);

		List<IRecord> records = new ArrayList<IRecord>();
		// log.debug("Rect: " + layout);
		records.add(createNewOrSetAttributeRecord(null,
				SVGConstants.SVG_X_ATTRIBUTE, String.valueOf(layout.x),
				onlyCreateNewRecords));
		records.add(createNewOrSetAttributeRecord(null,
				SVGConstants.SVG_Y_ATTRIBUTE, String.valueOf(layout.y),
				onlyCreateNewRecords));
		records.add(createNewOrSetAttributeRecord(null,
				SVGConstants.SVG_WIDTH_ATTRIBUTE, String.valueOf(a),
				onlyCreateNewRecords));
		records.add(createNewOrSetAttributeRecord(null,
				SVGConstants.SVG_HEIGHT_ATTRIBUTE, String.valueOf(a),
				onlyCreateNewRecords));
		records.add(createNewOrSetAttributeRecord(null,
				SVGConstants.SVG_TEXT_VALUE, String.valueOf(this.getText()),
				onlyCreateNewRecords));
		return records;
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
}
