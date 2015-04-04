package de.fu_berlin.inf.dpp.intellij.ui.swt_browser;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.wm.WindowManager;
import de.fu_berlin.inf.dpp.core.Saros;
import de.fu_berlin.inf.dpp.ui.manager.IDialogManager;
import de.fu_berlin.inf.dpp.ui.view_parts.BrowserPage;

import javax.swing.JDialog;
import javax.swing.JFrame;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements the dialog manager for the IntelliJ platform.
 */
public class IntelliJDialogManager implements IDialogManager {

    private Saros saros;

    private Map<String, JDialog> openDialogs = new HashMap<String, JDialog>();

    /**
     * TODO just inject project object instead of Saros
     * @param saros Saros object (needed to display dialog
     *              in the IntelliJ Window)
     */
    public IntelliJDialogManager(Saros saros) {
        this.saros = saros;
    }

    @Override
    public void showDialogWindow(final BrowserPage startPage) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
                if (!openDialogs.containsKey(startPage.getWebpage())) {
                    JFrame parent = WindowManager.getInstance()
                        .getFrame(saros.getProject());
                    JDialog jDialog = new JDialog(parent);
                    jDialog.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent e) {
                            openDialogs.remove(startPage.getWebpage());
                        }
                    });
                        SwtBrowserCanvas browser = new SwtBrowserCanvas(startPage);
                    jDialog.setSize(600, 600);
                    centerWindowToScreen(jDialog);
                    jDialog.add(browser);
                    jDialog.setVisible(true);
                    browser.launchBrowser();
                    openDialogs.put(startPage.getWebpage(), jDialog);
                }
            }
        });
    }

    @Override
    public void closeDialogWindow(final String webPage) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                if (openDialogs.containsKey(webPage)) {
                    JDialog jDialog = openDialogs.get(webPage);
                    if (jDialog != null) {
                        //TODO verify that this is sufficient
                        jDialog.dispose();
                    }
                    openDialogs.remove(webPage);
                }
            }
        });
    }

    private void centerWindowToScreen(Window w) {
        Rectangle screen = w.getGraphicsConfiguration().getBounds();
        w.setLocation(screen.x + (screen.width - w.getWidth()) / 2,
            screen.y + (screen.height - w.getHeight()) / 2);
    }
}
