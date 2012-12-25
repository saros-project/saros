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
import de.fu_berlin.inf.nebula.utils.ColorUtils;

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
     * Scale by which the user color's lightness should be modified for the
     * viewport and highlighting.
     */
    private static final float LIGHTNESS_SCALE = 0.12f;

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
     * Returns the default lightness scale.<br />
     * Can be used in combination with
     * {@link ColorUtils#addLightness(RGB,float)} to get a slightly lighter
     * color of the default one.
     * 
     * @return
     */
    public static float getLightnessScale() {
        return LIGHTNESS_SCALE;
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

        // TODO This should not depend on the ContributionAnnotation, but be
        // configurable like all colors!
        String annotationType = ContributionAnnotation.TYPE + "."
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
     * Sets the color of given user.
     * <p>
     * More precisely the three different annotation types selection, viewport
     * and annotation are set. Whereby a slightly different shade of color is
     * used for the selection annotation.
     * 
     * @param user
     * @param userRGB
     */
    public static void setUserColor(User user, final RGB userRGB) {
        int colorID = user.getColorID();

        // TODO This should not depend on the SelectionAnnotation, but be
        // configurable like all colors!
        String annotationTypeForSelection = SelectionAnnotation.TYPE + "."
            + String.valueOf(colorID + 1);
        String annotationTypeForViewport = ViewportAnnotation.TYPE + "."
            + String.valueOf(colorID + 1);
        String annotationTypeForContribution = ContributionAnnotation.TYPE
            + "." + String.valueOf(colorID + 1);

        AnnotationPreferenceLookup lookup = EditorsUI
            .getAnnotationPreferenceLookup();

        final AnnotationPreference apForSelection = lookup
            .getAnnotationPreference(annotationTypeForSelection);

        final AnnotationPreference apForViewPort = lookup
            .getAnnotationPreference(annotationTypeForViewport);

        final AnnotationPreference apForContribution = lookup
            .getAnnotationPreference(annotationTypeForContribution);

        if (apForSelection == null) {
            return;
        }

        if (apForViewPort == null) {
            return;
        }

        if (apForContribution == null) {
            return;
        }

        Utils.runSafeSWTSync(log, new Runnable() {
            @Override
            public void run() {
                RGB scaledUserRGB = ColorUtils.addLightness(userRGB,
                    LIGHTNESS_SCALE);

                log.info("Set new User Color: " + userRGB);
                log.debug("Set new User Color (scaled): " + scaledUserRGB);

                // selection color
                // (highlighting and cursor position)
                EditorsUI.getPreferenceStore().setValue(
                    apForSelection.getColorPreferenceKey(),
                    scaledUserRGB.red + "," + scaledUserRGB.green + ","
                        + scaledUserRGB.blue);

                // viewport color
                // (side bar, displays what a participant is looking at)
                EditorsUI.getPreferenceStore().setValue(
                    apForViewPort.getColorPreferenceKey(),
                    userRGB.red + "," + userRGB.green + "," + userRGB.blue);

                // contribution color
                // (the last editor changes made by a participant)
                EditorsUI.getPreferenceStore().setValue(
                    apForContribution.getColorPreferenceKey(),
                    userRGB.red + "," + userRGB.green + "," + userRGB.blue);
            }
        });
    }
}
