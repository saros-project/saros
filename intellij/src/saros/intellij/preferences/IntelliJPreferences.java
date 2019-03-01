package saros.intellij.preferences;

import saros.preferences.PreferenceInitializer;
import saros.preferences.Preferences;

/**
 * IntelliJ implementation of the {@link Preferences} abstract class, that uses an {@link
 * PropertiesComponentAdapter}.
 *
 * <p>Preferences that are custom to IntelliJ may be defined here.
 */
public class IntelliJPreferences extends Preferences {

  public IntelliJPreferences(PropertiesComponentAdapter store) {
    super(store);
    PreferenceInitializer.initialize(store);
  }
}
