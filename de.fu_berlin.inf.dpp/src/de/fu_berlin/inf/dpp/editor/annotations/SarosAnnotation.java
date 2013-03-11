package de.fu_berlin.inf.dpp.editor.annotations;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.AnnotationPreferenceLookup;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.project.internal.SarosSession;
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

        if (isNumbered)
            setType(getNumberedType(type, source.getColorID()));
    }

    public User getSource() {
        return source;
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
     * @return the corresponding color
     */
    public static Color getUserColor(User user) {
        return getUserColor(user.getColorID());
    }

    /**
     * Returns the color that corresponds to a user.
     * <p>
     * <b>Important notice:</b> Every returned color instance allocates OS
     * resources that need to be disposed with {@link Color#dispose()}!
     * 
     * @param colorID
     *            the color id of the user
     * @return the corresponding color
     */
    public static Color getUserColor(int colorID) {
        return getColor(ContributionAnnotation.TYPE, colorID);
    }

    /**
     * Returns the color that corresponds to a user.
     * <p>
     * <b>Important notice:</b> Every returned color instance allocates OS
     * resources that need to be disposed with {@link Color#dispose()}!
     * 
     * @param type
     * @param colorID
     * @return the corresponding color or a default one if no color is stored
     */
    protected static Color getColor(String type, int colorID) {

        type = getNumberedType(type, colorID);

        AnnotationPreferenceLookup lookup = EditorsUI
            .getAnnotationPreferenceLookup();

        AnnotationPreference ap = lookup.getAnnotationPreference(type);

        if (ap == null)
            throw new RuntimeException(
                "could not read color value of annotation '" + type
                    + "' because it does not exists");

        RGB rgb = PreferenceConverter.getColor(EditorsUI.getPreferenceStore(),
            ap.getColorPreferenceKey());

        return new Color(Display.getDefault(), rgb);
    }

    /**
     * Loads the colors from the plugin.xml and overwrites possible errors in
     * the PreferenceStore
     */
    public static void resetColors() {
        for (int i = 0; i <= SarosSession.MAX_USERCOLORS; ++i) {

            AnnotationPreferenceLookup lookup = EditorsUI
                .getAnnotationPreferenceLookup();

            RGB selectionRGB = readRGB(lookup, i, SelectionAnnotation.TYPE);
            RGB viewportRGB = readRGB(lookup, i, ViewportAnnotation.TYPE);
            RGB contributionRGB = readRGB(lookup, i,
                ContributionAnnotation.TYPE);

            setColor(i, selectionRGB, contributionRGB, viewportRGB);
        }
    }

    private static RGB readRGB(AnnotationPreferenceLookup lookup, int i,
        String type) {

        type = getNumberedType(type, i);

        AnnotationPreference ap = lookup.getAnnotationPreference(type);

        if (ap == null)
            throw new RuntimeException("annotation preference '" + type
                + "' does not exists");

        return ap.getColorPreferenceValue();
    }

    /**
     * Sets the color of colorID.
     * <p>
     * More precisely the three different annotation types selection, viewport
     * and contribution annotation are set.
     * 
     * @param colorID
     * @param selectionRGB
     * @param contributionRGB
     * @param viewportRGB
     */
    private static void setColor(int colorID, final RGB selectionRGB,
        final RGB contributionRGB, final RGB viewportRGB) {

        String annotationTypeForSelection = getNumberedType(
            SelectionAnnotation.TYPE, colorID);

        String annotationTypeForViewport = getNumberedType(
            ViewportAnnotation.TYPE, colorID);

        String annotationTypeForContribution = getNumberedType(
            ContributionAnnotation.TYPE, colorID);

        AnnotationPreferenceLookup lookup = EditorsUI
            .getAnnotationPreferenceLookup();

        AnnotationPreference apForSelection = lookup
            .getAnnotationPreference(annotationTypeForSelection);

        AnnotationPreference apForViewPort = lookup
            .getAnnotationPreference(annotationTypeForViewport);

        AnnotationPreference apForContribution = lookup
            .getAnnotationPreference(annotationTypeForContribution);

        if (apForSelection == null || apForViewPort == null
            || apForContribution == null) {
            log.error("could not set colors for color id " + colorID
                + " because at least one annotation preference does not exists");
            return;
        }

        if (selectionRGB == null || viewportRGB == null
            || contributionRGB == null) {
            log.error("could not set colors for color id " + colorID
                + " because at least one color value does not exists");
            return;
        }

        log.debug("set color values [c:" + contributionRGB + "|s:"
            + selectionRGB + "|v:" + viewportRGB + "] for color id: " + colorID);

        IPreferenceStore editorsUIStore = EditorsUI.getPreferenceStore();

        // selection color
        // (highlighting and cursor position)
        PreferenceConverter.setValue(editorsUIStore,
            apForSelection.getColorPreferenceKey(), selectionRGB);

        // viewport color
        // (side bar, displays what a participant is looking at)

        PreferenceConverter.setValue(editorsUIStore,
            apForViewPort.getColorPreferenceKey(), viewportRGB);

        // contribution color
        // (the last editor changes made by a participant)
        PreferenceConverter.setValue(editorsUIStore,
            apForContribution.getColorPreferenceKey(), contributionRGB);
    }

    private static String getNumberedType(String type, int colorID) {
        return type + "." + getColorIDSuffix(colorID);
    }

    private static String getColorIDSuffix(int colorID) {
        if (colorID < 0 || colorID >= SarosSession.MAX_USERCOLORS)
            return "default";

        return String.valueOf(colorID + 1);
    }
}
