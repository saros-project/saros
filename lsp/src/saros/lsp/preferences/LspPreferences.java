package saros.lsp.preferences;

import saros.preferences.IPreferenceStore;
import saros.preferences.Preferences;

/** Preferences of the Saros language server. */
public class LspPreferences extends Preferences {

  public LspPreferences(IPreferenceStore store) {
    super(store);
  }
}
