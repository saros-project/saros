package saros.intellij.ui.swt_browser;

import com.intellij.openapi.wm.WindowManager;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JDialog;
import saros.intellij.ui.util.UIProjectUtils;
import saros.synchronize.UISynchronizer;
import saros.ui.ide_embedding.DialogManager;
import saros.ui.ide_embedding.IBrowserDialog;
import saros.ui.pages.IBrowserPage;

/** Implements the dialog manager for the IntelliJ platform. */
public class IntelliJDialogManager extends DialogManager {

  private UIProjectUtils projectUtils;

  public IntelliJDialogManager(UISynchronizer uiSynchronizer, UIProjectUtils projectUtils) {
    super(uiSynchronizer);

    this.projectUtils = projectUtils;
  }

  @Override
  protected IBrowserDialog createDialog(final IBrowserPage startPage) {
    JDialog jDialog = new JDialog();

    projectUtils.runWithProject(
        project -> jDialog.setLocationRelativeTo(WindowManager.getInstance().getFrame(project)));

    jDialog.addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosing(WindowEvent e) {
            removeDialogEntry(startPage.getRelativePath());
          }
        });
    jDialog.setSize(600, 600);
    IntelliJBrowserDialog dialog = new IntelliJBrowserDialog(jDialog);
    SwtBrowserCanvas browser = new SwtBrowserCanvas(startPage);
    jDialog.add(browser);
    jDialog.setVisible(true);
    browser.launchBrowser();

    return dialog;
  }
}
