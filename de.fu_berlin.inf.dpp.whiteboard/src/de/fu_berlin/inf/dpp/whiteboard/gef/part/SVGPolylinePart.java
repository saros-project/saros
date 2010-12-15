package de.fu_berlin.inf.dpp.whiteboard.gef.part;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Polyline;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.swt.graphics.Color;

import de.fu_berlin.inf.dpp.whiteboard.gef.model.SVGPolylineRecord;

public class SVGPolylinePart extends ElementRecordPart {

	@Override
	protected IFigure createFigure() {
		Polyline figure = new Polyline();

		SVGPolylineRecord record = (SVGPolylineRecord) getElementRecord();
		figure.setPoints(record.getPoints().getCopy());

		XYLayout layout = new XYLayout();
		figure.setLayoutManager(layout);
		figure.setForegroundColor(new Color(null, 0, 0, 0));

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
				&& getElementRecord().isCommitted())
			getViewer().appendSelection(this);
	}

}
