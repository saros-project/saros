package de.fu_berlin.inf.dpp.editor.annotations;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.AnnotationPreferenceLookup;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * Abstract base class for {@link Annotation}s.
 * 
 * Configuration of the annotations is done in the plugin-xml.
 */
public abstract class SarosAnnotation extends Annotation {

    /** Source of this annotation (jabber id). */
    private User source;
    @SuppressWarnings("unused")
    private Logger log = Logger.getLogger(SarosAnnotation.class);

    /**
     * Creates a SarosAnnotation.
     * 
     * @param type
     *            of the {@link Annotation}.
     * @param isNumbered
     *            whether the type should be extended by the color ID of the
     *            source.
     * @param text
     *            for the tooltip.
     * @param source
     *            the user which created this annotation
     */
    SarosAnnotation(String type, boolean isNumbered, String text, User source) {
        super(type, false, text);
        this.source = source;

        if (isNumbered) {
            setType(type + "." + (source.getColorID() + 1));
        }
    }

    public User getSource() {
        return this.source;
    }

    public static Color getUserColor(User user) {

        int colorID = user.getColorID();

        // TODO This should depend on the SelectionAnnotation, but be
        // configurable like all colors!
        String annotationType = SelectionAnnotation.TYPE + "."
            + String.valueOf(colorID + 1);

        AnnotationPreferenceLookup lookup = EditorsUI
            .getAnnotationPreferenceLookup();
        AnnotationPreference ap = lookup
            .getAnnotationPreference(annotationType);
        if (ap == null) {
            return null;
        }

        RGB rgb;
        try {
            rgb = PreferenceConverter.getColor(EditorsUI.getPreferenceStore(),
                ap.getColorPreferenceKey());
        } catch (RuntimeException e) {
            return null;
        }

        return new Color(Display.getDefault(), rgb);
    }

    /**
     * @return a string with the given prefix followed by either the nickname of
     *         the given user or the name of the jabber ID of the user.
     */
    protected static String createLabel(String prefix, User user) {
        return prefix + " " + Util.getName(user);
    }
}
