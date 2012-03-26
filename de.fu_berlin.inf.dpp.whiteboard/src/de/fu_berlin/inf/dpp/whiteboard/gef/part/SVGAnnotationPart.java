package de.fu_berlin.inf.dpp.whiteboard.gef.part;

import org.apache.batik.util.SVGConstants;
import org.apache.log4j.Logger;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import de.fu_berlin.inf.dpp.whiteboard.gef.model.SVGAnnotationRecord;
import de.fu_berlin.inf.dpp.whiteboard.gef.model.SVGTextBoxRecord;

public class SVGAnnotationPart extends ElementRecordPart {
	public static final int IMAGE_WIDTH = 25;
	public static final int IMAGE_HEIGHT = 25;

	public static final int IMAGE_WIDTH_FEEDBACK = 25;
	public static final int IMAGE_HEIGHT_FEEDBACK = 25;
	private static final Image ICON = Display.getDefault().getSystemImage(
			SWT.ICON_INFORMATION);

	protected Logger log = Logger.getLogger(SVGAnnotationPart.class);

	@Override
	protected IFigure createFigure() {
		ImageFigure figure = new ImageFigure();

		SVGTextBoxRecord r = (SVGTextBoxRecord) getElementRecord();
		String s = null;

		if (r != null)
			s = r.getAttributeValue(SVGConstants.SVG_TEXT_VALUE);

		if (s == null || s.isEmpty())
			s = "New Annotation";

		figure.setImage(resize(ICON, IMAGE_WIDTH_FEEDBACK,
				IMAGE_HEIGHT_FEEDBACK));
		figure.setToolTip(new Label(s));
		// figure.setMinimumSize(new Dimension(100, 100));

		log.trace("Create Annotation Figure - Text: " + s);

		XYLayout layout = new XYLayout();
		figure.setLayoutManager(layout);

		return figure;

	}

	/**
	 * Checks if a text was entered by the user. If not (e.g. when creating if
	 * drag & drop) the dialog opens to enter a text
	 */
	protected void refreshText() {
		SVGTextBoxRecord r = (SVGTextBoxRecord) getElementRecord();
		IFigure figure = getFigure();
		if (r != null) {

			String s = r.getText(); // r.getAttributeValue(SVGConstants.SVG_TEXT_VALUE);
			String oldtext = ((Label) figure.getToolTip()).getText();

			s = oldtext;
			r.setText(oldtext);
			figure.setToolTip(new Label(s));

			log.trace("Refreshed Text: " + s);
		}
	}

	@Override
	protected void refreshVisuals() {

		refreshText();

		// Scale Image right
		Image i = ((ImageFigure) getFigure()).getImage();
		SVGAnnotationRecord r = (SVGAnnotationRecord) getModel();
		((ImageFigure) getFigure()).setImage(resize(i, r.getSize().width,
				r.getSize().height));
		super.refreshVisuals();
	}

	private Image resize(Image image, int width, int height) {
		/*
		 * TODO: Does image need dispose() too?!
		 */
		Image scaled = new Image(Display.getDefault(), width, height);
		GC gc = new GC(scaled);
		gc.setAntialias(SWT.ON);
		gc.setInterpolation(SWT.HIGH);
		gc.drawImage(image, 0, 0, image.getBounds().width,
				image.getBounds().height, 0, 0, width, height);
		gc.dispose();

		return scaled;
	}

}
