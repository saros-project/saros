package saros.ui.dialogs;

import org.apache.log4j.Logger;
import saros.ui.util.DialogUtils;
import saros.ui.util.SWTUtils;

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
