package de.fu_berlin.inf.dpp.ui.commandHandlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class OpenSarosPreferencesHandler extends AbstractHandler {

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    PreferenceDialog pref =
        PreferencesUtil.createPreferenceDialogOn(
            null, "de.fu_berlin.inf.dpp.preferences", null, null);
    if (pref != null) pref.open();

    return null;
  }
}
