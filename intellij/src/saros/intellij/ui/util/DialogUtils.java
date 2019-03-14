package saros.intellij.ui.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import java.awt.Component;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import saros.SarosPluginContext;
import saros.repackaged.picocontainer.annotations.Inject;

/** Dialog message helper that shows Dialogs in the current Thread. */
public class DialogUtils {
  private static final Logger LOG = Logger.getLogger(DialogUtils.class);

  @Inject private static Project project;

  private DialogUtils() {}

  static {
    SarosPluginContext.initComponent(new DialogUtils());
  }

  /**
   * Displays a confirmation dialog.
   *
   * @param parent the parent Component. If <code>parent</code> is null, the project's window is
   *     used.
   * @param title
   * @param msg
   * @return <code>true</code>, if OK was chosen, <code>false</code> otherwise
   * @deprecated use {@link #showConfirm(Project, String, String)} instead
   */
  @Deprecated
  public static boolean oldShowConfirm(Component parent, String title, String msg) {

    LOG.info("Showing confirmation dialog: " + title + " - " + msg);

    int resp =
        JOptionPane.showConfirmDialog(
            notNullOrDefaultParent(parent), msg, title, JOptionPane.OK_CANCEL_OPTION);

    return resp == JOptionPane.OK_OPTION;
  }

  /**
   * Displays a confirmation dialog.
   *
   * @param project the project used as a reference to generate and position the dialog
   * @param title the text displayed as the title of the dialog
   * @param msg the text displayed as the message of the dialog
   * @return <code>true</code>, if OK was chosen, <code>false</code> otherwise
   */
  public static boolean showConfirm(Project project, String title, String msg) {

    LOG.info("Showing confirmation dialog: " + title + " - " + msg);

    Component parentComponent = getProjectComponent(project);

    int resp =
        JOptionPane.showConfirmDialog(parentComponent, msg, title, JOptionPane.OK_CANCEL_OPTION);

    return resp == JOptionPane.OK_OPTION;
  }

  /**
   * Shows a Yes/No question dialog.
   *
   * @param project the project used as a reference to generate and position the dialog
   * @param title the text displayed as the title of the dialog
   * @param msg the text displayed as the message of the dialog
   * @return <code>true</code> if Yes was chosen, <code>false</code> otherwise
   */
  public static boolean showQuestion(Project project, String title, String msg) {

    LOG.info("Showing question dialog: " + title + " - " + msg);

    Component parentComponent = getProjectComponent(project);

    int answer =
        JOptionPane.showConfirmDialog(parentComponent, msg, title, JOptionPane.YES_NO_OPTION);

    return answer == JOptionPane.YES_OPTION;
  }

  private static Component notNullOrDefaultParent(Component parent) {
    return parent != null ? parent : WindowManager.getInstance().getFrame(project);
  }

  private static Component getProjectComponent(Project project) {
    return WindowManager.getInstance().getFrame(project);
  }
}
