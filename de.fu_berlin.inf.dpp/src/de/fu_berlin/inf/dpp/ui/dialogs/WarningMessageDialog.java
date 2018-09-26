package de.fu_berlin.inf.dpp.ui.dialogs;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import de.fu_berlin.inf.dpp.ui.util.DialogUtils;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;

public class WarningMessageDialog {

    private static final Logger log = LogManager
        .getLogger(WarningMessageDialog.class.getName());

    /**
     * Opens a modal dialog (with the given title) which displays the given
     * warning message to the user.
     */
    public static void showWarningMessage(final String title,
        final String message) {
        SWTUtils.runSafeSWTSync(log, new Runnable() {
            @Override
            public void run() {
                DialogUtils.openWarningMessageDialog(SWTUtils.getShell(),
                    title, message);
            }
        });
    }

}
