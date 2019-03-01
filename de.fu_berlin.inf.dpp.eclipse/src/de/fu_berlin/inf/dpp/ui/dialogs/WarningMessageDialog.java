package de.fu_berlin.inf.dpp.ui.dialogs;

import de.fu_berlin.inf.dpp.ui.util.DialogUtils;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import org.apache.log4j.Logger;

public class WarningMessageDialog {

  private static final Logger log = Logger.getLogger(WarningMessageDialog.class.getName());

  /**
   * Opens a modal dialog (with the given title) which displays the given warning message to the
   * user.
   */
  public static void showWarningMessage(final String title, final String message) {
    SWTUtils.runSafeSWTSync(
        log,
        new Runnable() {
          @Override
          public void run() {
            DialogUtils.openWarningMessageDialog(SWTUtils.getShell(), title, message);
          }
        });
  }
}
