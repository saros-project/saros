package saros.intellij.ui.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import java.awt.Component;
import java.awt.Container;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import org.picocontainer.annotations.Inject;
import saros.SarosPluginContext;

/** Dialog message helper that shows Dialogs in the current Thread. */
public class DialogUtils {
  private static final Logger LOG = Logger.getLogger(DialogUtils.class);

  @Inject private static Project project;

  private DialogUtils() {}

  static {
    SarosPluginContext.initComponent(new DialogUtils());
  }

  /**
   * Displays an error message.
   *
   * @param parent the parent Component. If <code>parent</code> is null, the project's window is
   *     used.
   * @param title
   * @param msg
   */
  public static void showError(Component parent, String title, String msg) {
    LOG.info("Showing error dialog: " + title + " - " + msg);

    JOptionPane.showMessageDialog(
        notNullOrDefaultParent(parent), msg, title, JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Displays a confirmation dialog.
   *
   * @param parent the parent Component. If <code>parent</code> is null, the project's window is
   *     used.
   * @param title
   * @param msg
   * @return <code>true</code>, if OK was chosen, <code>false</code> otherwise
   */
  public static boolean showConfirm(Component parent, String title, String msg) {

    LOG.info("Showing confirmation dialog: " + title + " - " + msg);

    int resp =
        JOptionPane.showConfirmDialog(
            notNullOrDefaultParent(parent), msg, title, JOptionPane.OK_CANCEL_OPTION);

    return resp == JOptionPane.OK_OPTION;
  }

  /**
   * Shows a Yes/No question dialog.
   *
   * @param parent the parent Component. If <code>parent</code> is null, the project's window is
   *     used.
   * @param title
   * @param msg
   * @return <code>true</code> if Yes was chosen, <code>false</code> otherwise
   */
  public static boolean showQuestion(Component parent, String title, String msg) {

    LOG.info("Showing question dialog: " + title + " - " + msg);

    int answer =
        JOptionPane.showConfirmDialog(
            notNullOrDefaultParent(parent), msg, title, JOptionPane.YES_NO_OPTION);

    return answer == JOptionPane.YES_OPTION;
  }

  /**
   * Shows an Info dialog.
   *
   * @param parent the parent Component. If <code>parent</code> is null, the project's window is
   *     used.
   * @param title
   * @param msg
   */
  public static void showInfo(Container parent, String title, String msg) {
    LOG.info("Showing info dialog: " + title + " - " + msg);

    JOptionPane.showMessageDialog(
        notNullOrDefaultParent(parent), msg, title, JOptionPane.INFORMATION_MESSAGE);
  }

  private static Component notNullOrDefaultParent(Component parent) {
    return parent != null ? parent : WindowManager.getInstance().getFrame(project);
  }
}
