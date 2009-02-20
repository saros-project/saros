package de.fu_berlin.inf.dpp.editor.annotations;

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
 * The annotation that shows the viewport of the driver.
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

        String annotationType = ViewportAnnotation.TYPE + "."
            + (getColorIdForUser(source) + 1);
        setType(annotationType);
        AnnotationPreferenceLookup lookup = EditorsUI
            .getAnnotationPreferenceLookup();
        AnnotationPreference annotationPreference = lookup
            .getAnnotationPreference(annotationType);
        RGB rgb = PreferenceConverter.getColor(EditorsUI.getPreferenceStore(),
            annotationPreference.getColorPreferenceKey());
        Display display = Display.getDefault();
        strokeColor = new Color(display, scaleColor(rgb, STROKE_SCALE));
        fillColor = new Color(display, scaleColor(rgb, FILL_SCALE));
    }

    public void paint(GC gc, Canvas canvas, Rectangle bounds) {
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

    public int getLayer() {
        return IAnnotationPresentation.DEFAULT_LAYER;
    }

    public void propertyChange(PropertyChangeEvent event) {
        /*
         * TODO [MR] This annotations color depends on the driver, so this
         * method should be obsolete.
         */
        assert false;
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

    protected RGB scaleColor(RGB rgb, double scale) {
        int red = (int) ((1.0 - scale) * rgb.red + 255 * scale);
        int green = (int) ((1.0 - scale) * rgb.green + 255 * scale);
        int blue = (int) ((1.0 - scale) * rgb.blue + 255 * scale);
        return new RGB(red, green, blue);
    }
}
