package de.fu_berlin.inf.dpp.intellij.ui.swt_browser;

import com.intellij.openapi.wm.WindowManager;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.core.Saros;
import de.fu_berlin.inf.dpp.synchronize.UISynchronizer;
import de.fu_berlin.inf.dpp.ui.ide_embedding.DialogManager;
import de.fu_berlin.inf.dpp.ui.ide_embedding.IBrowserDialog;
import de.fu_berlin.inf.dpp.ui.pages.IBrowserPage;

import javax.swing.JDialog;
import javax.swing.JFrame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Implements the dialog manager for the IntelliJ platform.
 */
public class IntelliJDialogManager extends DialogManager {

    private Saros saros;

    /**
     * TODO just inject project object instead of Saros
     *
     * @param saros Saros object (needed to display dialog
     *              in the IntelliJ Window)
     */
    public IntelliJDialogManager(Saros saros, UISynchronizer uiSynchronizer) {
        super(uiSynchronizer);
        this.saros = saros;
    }

    @Override
    protected IBrowserDialog createDialog(final IBrowserPage startPage) {
        JFrame parent = WindowManager.getInstance()
            .getFrame(saros.getProject());
        JDialog jDialog = new JDialog(parent);
        jDialog.addWindowListener(new WindowAdapter() {
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
