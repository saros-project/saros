package de.fu_berlin.inf.dpp.whiteboard.gef.part;

import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.swt.graphics.Color;

public class SVGRectPart extends ElementRecordPart {

	@Override
	protected IFigure createFigure() {
		IFigure figure = new RectangleFigure();

		XYLayout layout = new XYLayout();
		figure.setLayoutManager(layout);
		figure.setForegroundColor(ColorConstants.black);

		Border line = new LineBorder(new Color(null, 0, 0, 0));
		figure.setBorder(line);

		return figure;
	}

}
