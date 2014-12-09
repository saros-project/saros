/*
 *
 *  DPP - Serious Distributed Pair Programming
 *  (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2010
 *  (c) NFQ (www.nfq.com) - 2014
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 1, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * /
 */

package de.fu_berlin.inf.dpp.intellij.ui.swt_browser;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.wm.WindowManager;
import de.fu_berlin.inf.dpp.core.Saros;
import de.fu_berlin.inf.dpp.ui.manager.IDialogManager;

import javax.swing.JDialog;
import javax.swing.JFrame;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;

/**
 * Implements the dialog manager for the IntelliJ platform.
 */
public class IntelliJDialogManager implements IDialogManager {

    private Saros saros;

    private HashMap<String, JDialog> openDialogs = new HashMap<String, JDialog>();

    public IntelliJDialogManager(Saros saros) {
        this.saros = saros;
    }

    @Override
    public void showDialogWindow(final String startPage) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
                if (!openDialogs.containsKey(startPage)) {
                    JFrame parent = WindowManager.getInstance()
                        .getFrame(saros.getProject());
                    JDialog jDialog = new JDialog(parent);
                    jDialog.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent e) {
                            openDialogs.remove(startPage);
                        }
                    });
                    SwtBrowserCanvas browser = new SwtBrowserCanvas(
                        BrowserUtils.getUrlForClasspathFile(startPage));
                    jDialog.setSize(600, 600);
                    centerWindowToScreen(jDialog);
                    jDialog.add(browser);
                    jDialog.setVisible(true);
                    browser.launchBrowser();
                    openDialogs.put(startPage, jDialog);
                }
            }
        });
    }

    @Override
    public void closeDialogWindow(final String startPage) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                if (openDialogs.containsKey(startPage)) {
                    JDialog jDialog = openDialogs.get(startPage);
                    if (jDialog != null) {
                        //TODO verify that this is sufficient
                        jDialog.dispose();
                    }
                    openDialogs.remove(startPage);
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
