package de.fu_berlin.inf.dpp.editor.annotations;

import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.source.IAnnotationPresentation;
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
 * The annotation that shows the viewports of the drivers.
 * 
 * Configuration of this annotation is done in the plugin-xml.
 * 
 * @author rdjemili
 */
public class ViewportAnnotation extends SarosAnnotation implements
    IAnnotationPresentation {

    protected static final String TYPE = "de.fu_berlin.inf.dpp.annotations.viewport";

    public static final int LAYER = 6;

    private static final int INSET = 2;

    private static final double STROKE_SCALE = 0.5;

    private static final double FILL_SCALE = 0.9;

    private static Color strokeColor;

    private static Color fillColor;

    public ViewportAnnotation(String source) {
        super(ViewportAnnotation.TYPE, false, createLabel("Visible scope of",
            source), source);

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

    protected static RGB scaleColor(RGB rgb, double scale) {
        int red = (int) ((1.0 - scale) * rgb.red + 255 * scale);
        int green = (int) ((1.0 - scale) * rgb.green + 255 * scale);
        int blue = (int) ((1.0 - scale) * rgb.blue + 255 * scale);
        return new RGB(red, green, blue);
    }
}
