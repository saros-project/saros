package de.fu_berlin.inf.dpp.whiteboard.gef.part;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.XYLayout;

public class SVGEllipsePart extends ElementRecordPart {

	@Override
	protected IFigure createFigure() {
		// TODO provide custom Ellipse with elliptical border
		IFigure figure = new Ellipse();

		XYLayout layout = new XYLayout();
		figure.setLayoutManager(layout);
		figure.setForegroundColor(ColorConstants.black);

		return figure;
	}

}
