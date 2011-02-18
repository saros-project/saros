package de.fu_berlin.inf.dpp.ui.dialogs;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.ui.util.DialogUtils;
import de.fu_berlin.inf.dpp.util.Utils;

public class WarningMessageDialog {

    private static final Logger log = Logger
        .getLogger(WarningMessageDialog.class.getName());

    /**
     * Opens a modal dialog (with the given title) which displays the given
     * warning message to the user.
     */
    public static void showWarningMessage(final String title,
        final String message) {
        Utils.runSafeSWTSync(log, new Runnable() {
            public void run() {
                DialogUtils.openWarningMessageDialog(EditorAPI.getShell(),
                    title, message);
            }
        });
    }

}
