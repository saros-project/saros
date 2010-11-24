package de.fu_berlin.inf.dpp.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * Opens the Preferences Dialog with the Saros page selected.
 * 
 * @author haferburg
 */
public class OpenPreferencesAction implements IWorkbenchWindowActionDelegate {
    IWorkbenchWindow window;

    public void run(IAction action) {
        PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(window
            .getShell(), "de.fu_berlin.inf.dpp.preferences", null, null);
        if (pref != null)
            pref.open();
    }

    public void init(IWorkbenchWindow window) {
        this.window = window;
    }

    public void dispose() {
        // Nothing to dispose.
    }

    public void selectionChanged(IAction action, ISelection selection) {
        // Nothing to select.
    }

}
