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
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * Abstract base class for {@link Annotation}s.
 * 
 * Configuration of the annotations is done in the plugin-xml.
 */
public abstract class SarosAnnotation extends Annotation {
    private static final Logger log = Logger.getLogger(SarosAnnotation.class);

    /**
     * Source of this annotation (JID).
     * 
     * All access should use getSource()
     */
    private User source;

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
    public SarosAnnotation(String type, boolean isNumbered, String text,
        User source) {
        super(type, false, text);
        this.source = source;

        if (isNumbered) {
            setType(type + "." + (source.getColorID() + 1));
        }
    }

    public User getSource() {
        return this.source;
    }

    /**
     * Returns the color that corresponds to a user.
     * <p>
     * <b>Important notice:</b> Every returned color instance allocates OS
     * resources that need to be disposed with {@link Color#dispose()}!
     * 
     * @param user
     * @return the corresponding color, <code>null</code> if no color is stored
     */
    public static Color getUserColor(User user) {

        int colorID = user.getColorID();

        // TODO This should not depend on the SelectionAnnotation, but be
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

    public static void setUserColor(User user, final RGB userRGB) {
        int colorID = user.getColorID();

        // TODO This should not depend on the SelectionAnnotation, but be
        // configurable like all colors!
        String annotationType = SelectionAnnotation.TYPE + "."
            + String.valueOf(colorID + 1);
        String annotationTypeForViewPort = ViewportAnnotation.TYPE + "."
            + String.valueOf(colorID + 1);
        String annotationTypeForContribution = ContributionAnnotation.TYPE
            + "." + String.valueOf(colorID + 1);

        AnnotationPreferenceLookup lookup = EditorsUI
            .getAnnotationPreferenceLookup();

        final AnnotationPreference ap = lookup
            .getAnnotationPreference(annotationType);

        final AnnotationPreference apForViewPort = lookup
            .getAnnotationPreference(annotationTypeForViewPort);

        final AnnotationPreference apForContribution = lookup
            .getAnnotationPreference(annotationTypeForContribution);

        if (ap == null) {
            return;
        }

        if (apForViewPort == null) {
            return;
        }

        if (apForContribution == null) {
            return;
        }

        Utils.runSafeSWTSync(log, new Runnable() {
            public void run() {
                int r = userRGB.red;
                int g = userRGB.green;
                int b = userRGB.blue;

                log.info("Set new User Color: " + userRGB);
                // Highlighting color of buddy
                EditorsUI.getPreferenceStore().setValue(
                    ap.getColorPreferenceKey(), r + "," + g + "," + b);

                // viewport color (side bar, displays what buddy is looking
                // at)
                EditorsUI.getPreferenceStore().setValue(
                    apForViewPort.getColorPreferenceKey(),
                    r + "," + g + "," + b);

                // color for things, that the buddy writes
                // make a small different color for selection and
                // contribution color
                EditorsUI.getPreferenceStore().setValue(
                    apForContribution.getColorPreferenceKey(),
                    (int) (r * 0.9) + "," + (int) (g * 0.9) + ","
                        + (int) (b * 0.9));
            }
        });
    }
}
