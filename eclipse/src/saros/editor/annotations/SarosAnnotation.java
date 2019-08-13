package saros.editor.annotations;

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
import saros.session.User;

/**
 * Abstract base class for {@link Annotation}s.
 *
 * <p>Configuration of the annotations is done in the plugin-xml.
 */
public abstract class SarosAnnotation extends Annotation {
  private static final Logger log = Logger.getLogger(SarosAnnotation.class);

  /**
   * Constant representing the amount of configured annotations for each Saros annotation type in
   * the plugin.xml file.
   */
  public static final int SIZE = 5;

  /**
   * Source of this annotation (JID).
   *
   * <p>All access should use getSource()
   */
  private User source;

  /**
   * Creates a SarosAnnotation.
   *
   * @param type of the {@link Annotation}.
   * @param isNumbered whether the type should be extended by the color ID of the source.
   * @param text for the tooltip.
   * @param source the user which created this annotation
   */
  public SarosAnnotation(String type, boolean isNumbered, String text, User source) {
    super(type, false, text);
    this.source = source;

    if (isNumbered) setType(getNumberedType(type, source.getColorID()));
  }

  public User getSource() {
    return source;
  }

  /**
   * Shorthand for <code>getUserColor(user.getColorID())</code>.
   *
   * <p><b>Important notice:</b> Every returned color instance allocates OS resources that need to
   * be disposed with {@link Color#dispose()}!
   *
   * @see #getUserColor(int)
   */
  public static Color getUserColor(User user) {
    return getUserColor(user.getColorID());
  }

  /**
   * Returns the color that corresponds to a user.
   *
   * <p><b>Important notice:</b> Every returned color instance allocates OS resources that need to
   * be disposed with {@link Color#dispose()}!
   *
   * @param colorID the color id of the user
   * @return the corresponding color
   */
  public static Color getUserColor(int colorID) {
    return getColor(ContributionAnnotation.TYPE, colorID);
  }

  /**
   * Returns the color for the given annotation type and user color id.
   *
   *<p>E.g: <code>getColor("saros.annotations.viewport", 2)
   * <p><b>Important notice:</b> Every returned color instance allocates OS resources that need to
   * be disposed with {@link Color#dispose()}!
   *
   * @see Annotation#getType()
   * @param type annotation type as defined in the plugin.xml
   * @param colorId the color id
   * @return the corresponding color or a default one if no color is stored
   */
  public static Color getColor(final String type, final int colorId) {

    final String typeToLookup = getNumberedType(type, colorId);

    final AnnotationPreference ap =
        EditorsUI.getAnnotationPreferenceLookup().getAnnotationPreference(typeToLookup);

    if (ap == null)
      throw new IllegalArgumentException(
          "could not read color value of annotation '"
              + typeToLookup
              + "' because it does not exists");

    RGB rgb =
        PreferenceConverter.getColor(EditorsUI.getPreferenceStore(), ap.getColorPreferenceKey());

    if (rgb == PreferenceConverter.COLOR_DEFAULT_DEFAULT) { // NOPMD
      rgb = ap.getColorPreferenceValue();
    }

    if (rgb == null)
      throw new IllegalArgumentException(
          "annotation '" + typeToLookup + "' does not have a default color");

    return new Color(Display.getDefault(), rgb);
  }

  /**
   * Loads the default colors from the plugin.xml and overwrites possible errors in the EditorsUI
   * preference store.
   */
  public static void resetColors() {

    log.debug("resetting annotation colors");

    final IPreferenceStore preferenceStore = EditorsUI.getPreferenceStore();

    final AnnotationPreferenceLookup lookup = EditorsUI.getAnnotationPreferenceLookup();

    final String[] annotationTypesToReset = {
      SelectionAnnotation.TYPE, ViewportAnnotation.TYPE, ContributionAnnotation.TYPE
    };

    for (int i = 0; i <= SIZE; ++i) {

      for (String annotationType : annotationTypesToReset) {

        final String annotationTypeToLookup = getNumberedType(annotationType, i);

        final AnnotationPreference preference =
            lookup.getAnnotationPreference(annotationTypeToLookup);

        if (preference == null) {
          log.warn("could not reset color for annotation type: " + annotationTypeToLookup);
          continue;
        }

        preferenceStore.setToDefault(preference.getColorPreferenceKey());

        if (log.isTraceEnabled()) {
          log.trace(
              "reset "
                  + annotationTypeToLookup
                  + " to: "
                  + preferenceStore.getString(preference.getColorPreferenceKey()));
        }
      }
    }
  }

  /**
   * Creates a per-user type from a base type and a color id.
   *
   * <p>Typically used to distinguish annotations stemming from different users (in contexts where
   * the user himself is not important, but his color is).
   *
   * @param type The type String of an annotation without a user- (or color-)specification
   * @param colorID The specificator. This method can cope with non-color ids.
   */
  public static String getNumberedType(final String type, final int colorID) {

    final String colorIDSuffix;

    if (colorID < 0 || colorID >= SIZE) colorIDSuffix = "default";
    else colorIDSuffix = String.valueOf(colorID + 1);

    return type + "." + colorIDSuffix;
  }
}
