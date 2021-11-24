package saros.ui.handlers.menu;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class OpenSarosPreferencesHandler {

  @Execute
  public Object execute() {
    PreferenceDialog pref =
        PreferencesUtil.createPreferenceDialogOn(null, "saros.preferences", null, null);
    if (pref != null) pref.open();
    return null;
  }
}
