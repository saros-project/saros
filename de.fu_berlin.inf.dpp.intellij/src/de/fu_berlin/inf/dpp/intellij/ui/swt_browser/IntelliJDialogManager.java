package de.fu_berlin.inf.dpp.intellij.ui.swt_browser;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import de.fu_berlin.inf.dpp.synchronize.UISynchronizer;
import de.fu_berlin.inf.dpp.ui.ide_embedding.DialogManager;
import de.fu_berlin.inf.dpp.ui.ide_embedding.IBrowserDialog;
import de.fu_berlin.inf.dpp.ui.pages.IBrowserPage;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JDialog;
import javax.swing.JFrame;

/** Implements the dialog manager for the IntelliJ platform. */
public class IntelliJDialogManager extends DialogManager {

  private Project project;

  public IntelliJDialogManager(UISynchronizer uiSynchronizer, Project project) {
    super(uiSynchronizer);

    this.project = project;
  }

  @Override
  protected IBrowserDialog createDialog(final IBrowserPage startPage) {
    JFrame parent = WindowManager.getInstance().getFrame(project);
    JDialog jDialog = new JDialog(parent);
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
