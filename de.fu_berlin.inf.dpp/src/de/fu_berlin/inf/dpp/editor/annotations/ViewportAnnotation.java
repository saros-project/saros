package de.fu_berlin.inf.dpp.editor.annotations;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.source.IAnnotationPresentation;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.AnnotationPreferenceLookup;

/**
 * The annotation that shows were the driver currently is.
 * 
 * Preferences are set in the plug-in XML
 * 
 * @author rdjemili
 */
public class ViewportAnnotation extends AnnotationSaros implements
	IAnnotationPresentation, IPropertyChangeListener {

    public static final String TYPE = "de.fu_berlin.inf.dpp.annotations.viewport";

    public static final int LAYER = 6;

    private static final int INSET = 2;

    private static final double STROKE_SCALE = 0.5;

    private static final double FILL_SCALE = 0.9;

    private static Color strokeColor;

    private static Color fillColor;

    public ViewportAnnotation(String label, String source) {
	super(ViewportAnnotation.TYPE, false, label, source);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.text.source.IAnnotationPresentation
     */
    public void paint(GC gc, Canvas canvas, Rectangle bounds) {
	if (ViewportAnnotation.strokeColor == null) {
	    ViewportAnnotation.strokeColor = getColor(ViewportAnnotation.STROKE_SCALE);
	    ViewportAnnotation.fillColor = getColor(ViewportAnnotation.FILL_SCALE);
	}

	Point canvasSize = canvas.getSize();

	gc.setBackground(ViewportAnnotation.fillColor);
	gc.setForeground(ViewportAnnotation.strokeColor);
	gc.setLineWidth(1);

	int x = ViewportAnnotation.INSET;
	int y = bounds.y;
	int w = canvasSize.x - 2 * ViewportAnnotation.INSET;
	int h = bounds.height;

	if (y < 0) {
	    h = h + y;
	    y = 0;
	}

	if (h <= 0) {
	    return;
	}

	gc.fillRectangle(x, y, w, h);
	gc.drawRectangle(x, y, w, h);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.text.source.IAnnotationPresentation
     */
    public int getLayer() {
	return IAnnotationPresentation.DEFAULT_LAYER;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.util.IPropertyChangeListener
     */
    public void propertyChange(PropertyChangeEvent event) {
	if (event.getProperty().equals(
		ViewportAnnotation.getColorPreferenceKey())) {
	    if (ViewportAnnotation.strokeColor != null) {
		ViewportAnnotation.strokeColor.dispose();
		ViewportAnnotation.strokeColor = null;

		ViewportAnnotation.fillColor.dispose();
		ViewportAnnotation.fillColor = null;
	    }
	}
    }

    public static String getColorPreferenceKey() {
	AnnotationPreferenceLookup lookup = EditorsUI
		.getAnnotationPreferenceLookup();

	AnnotationPreference preference = lookup
		.getAnnotationPreference(ViewportAnnotation.TYPE);

	return preference.getColorPreferenceKey();
    }

    private Color getColor(double scale) {
	IPreferenceStore store = EditorsUI.getPreferenceStore();
	store.addPropertyChangeListener(this);

	RGB rgb = PreferenceConverter.getColor(store, ViewportAnnotation
		.getColorPreferenceKey());
	int red = (int) ((1.0 - scale) * rgb.red + 255 * scale);
	int green = (int) ((1.0 - scale) * rgb.green + 255 * scale);
	int blue = (int) ((1.0 - scale) * rgb.blue + 255 * scale);
	rgb = new RGB(red, green, blue);

	return new Color(Display.getDefault(), rgb);
    }
}
