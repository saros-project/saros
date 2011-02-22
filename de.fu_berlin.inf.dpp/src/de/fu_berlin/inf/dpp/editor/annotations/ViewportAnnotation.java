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

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.util.ColorUtils;

/**
 * The annotation that shows the viewports of users with
 * {@link User.Permission#WRITE_ACCESS}.
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

    private static final float STROKE_SCALE = 1.05f;

    private static final float FILL_SCALE = 1.22f;

    private Color strokeColor;

    private Color fillColor;

    public ViewportAnnotation(User source) {
        super(ViewportAnnotation.TYPE, false, "Visible scope of "
            + source.getHumanReadableName(), source);

        String annotationType = ViewportAnnotation.TYPE + "."
            + (source.getColorID() + 1);
        setType(annotationType);

        AnnotationPreferenceLookup lookup = EditorsUI
            .getAnnotationPreferenceLookup();
        AnnotationPreference annotationPreference = lookup
            .getAnnotationPreference(annotationType);
        RGB rgb = PreferenceConverter.getColor(EditorsUI.getPreferenceStore(),
            annotationPreference.getColorPreferenceKey());

        Display display = Display.getDefault();
        strokeColor = new Color(display, ColorUtils.scaleColorBy(rgb,
            STROKE_SCALE));
        // FIXME: dispose strokeColor somewhere
        fillColor = new Color(display, ColorUtils.scaleColorBy(rgb, FILL_SCALE));
        // FIXME: dispose fillColor somewhere
    }

    public void paint(GC gc, Canvas canvas, Rectangle bounds) {
        Point canvasSize = canvas.getSize();

        gc.setBackground(fillColor);
        gc.setForeground(strokeColor);
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
}
