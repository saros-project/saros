package de.fu_berlin.inf.dpp.whiteboard.gef.util;

import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Polyline;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

/**
 * Static class that creates provides the icon figures and transforms them to an
 * image
 * 
 * @author jurke
 * 
 */
public class IconUtils {

	private static Image imageBuffer = null;
	private static GC imageGC = null;
	private static SWTGraphics swtGraphics = null;

	private static int LOW_WIDTH = 20;
	private static int LOW_HEIGHT = 16;
	private static int inset = 2;

	private static Color blue = new Color(null, 0, 0, 255);
	private static Color red = new Color(null, 255, 0, 0);
	private static Color black = new Color(null, 0, 0, 0);

	private static Image rect = null;
	private static Image line = null;
	private static Image ellipse = null;

	public static Image getRectImage() {
		if (rect == null) {
			rect = getIconImage(getRectangleIconFigure());
		}
		return rect;
	}

	public static Image getPencilImage() {
		if (line == null) {
			line = getIconImage(getPencilIconFigure());
		}
		return line;
	}

	public static Image getEllipseImage() {
		if (ellipse == null) {
			ellipse = getIconImage(getEllipseIconFigure());
		}
		return ellipse;
	}

	private static Image getIconImage(IFigure figure) {
		imageBuffer = new Image(null, LOW_WIDTH, LOW_HEIGHT);
		imageGC = new GC(imageBuffer, (SWT.RIGHT_TO_LEFT | SWT.LEFT_TO_RIGHT));
		swtGraphics = new SWTGraphics(imageGC);
		figure.paint(swtGraphics);
		return imageBuffer;
	}

	private static RectangleFigure getRectangleIconFigure() {
		RectangleFigure rect = new RectangleFigure();
		rect.setBackgroundColor(red);
		rect.setForegroundColor(blue);
		rect.setBounds(new Rectangle(inset, inset, LOW_WIDTH - 2 * inset,
				LOW_HEIGHT - 2 * inset));
		return rect;
	}

	private static Ellipse getEllipseIconFigure() {
		Ellipse ellipse = new Ellipse();
		ellipse.setBackgroundColor(red);
		ellipse.setForegroundColor(blue);
		ellipse.setBounds(new Rectangle(inset, inset, LOW_WIDTH - 2 * inset,
				LOW_HEIGHT - 2 * inset));
		return ellipse;
	}

	private static Polyline getPencilIconFigure() {
		Polyline line = new Polyline();
		int[] rawPoints = { 11, 3, 6, 3, 6, 4, 3, 4, 3, 5, 2, 5, 1, 6, 1, 8, 2,
				8, 2, 9, 3, 9, 4, 9, 5, 8, 7, 8, 7, 7, 8, 7, 8, 6, 13, 6, 14,
				7, 14, 10, 13, 10, 13, 11, 9, 11, 9, 12, 7, 12 };
		PointList points = new PointList(rawPoints);
		line.setPoints(points);
		line.setForegroundColor(black);
		return line;
	}
}
