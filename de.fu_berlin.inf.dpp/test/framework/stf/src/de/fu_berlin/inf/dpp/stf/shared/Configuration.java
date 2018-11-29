package de.fu_berlin.inf.dpp.stf.shared;

import java.util.Arrays;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * This class was auto-generated by Eclipse and is used to access the strings in
 * messages.properties.
 */
public class Configuration {

  private static final String[] BUNDLE_NAMES = {
    "de.fu_berlin.inf.dpp.project.messages",
    "de.fu_berlin.inf.dpp.feedback.messages",
    "de.fu_berlin.inf.dpp.ui.messages",
    "de.fu_berlin.inf.dpp.stf.shared.configuration"
  };

  private static final ResourceBundle[] RESOURCE_BUNDLES;

  static {
    RESOURCE_BUNDLES = new ResourceBundle[BUNDLE_NAMES.length];

    for (int i = 0; i < BUNDLE_NAMES.length; i++)
      RESOURCE_BUNDLES[i] = ResourceBundle.getBundle(BUNDLE_NAMES[i], Locale.ENGLISH);
  }

  private Configuration() {
    // do nothing
  }

  /**
   * Returns the value for the given key.
   *
   * @param key
   * @return the value for the key as a String or <code>null</code> if the value could not been
   *     found
   */
  public static String get(String key) {

    String value = null;

    assert RESOURCE_BUNDLES.length > 0;

    for (ResourceBundle bundle : RESOURCE_BUNDLES) {

      try {
        value = null;
        value = bundle.getString(key);
        if (value != null) return value;

      } catch (MissingResourceException e) {
        continue;
      }
    }

    /*
     * do not throw an exception as this will cause the JVM to throw an
     * error during class initialization in the static part segments
     */
    System.err.println(
        "key '"
            + key
            + "' was not found in any of the following bundles: "
            + Arrays.toString(BUNDLE_NAMES));

    return null;
  }
}
