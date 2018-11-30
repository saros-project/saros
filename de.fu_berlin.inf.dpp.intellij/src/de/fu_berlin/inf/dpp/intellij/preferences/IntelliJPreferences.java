package de.fu_berlin.inf.dpp.intellij.preferences;

import de.fu_berlin.inf.dpp.preferences.PreferenceInitializer;
import de.fu_berlin.inf.dpp.preferences.Preferences;

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
