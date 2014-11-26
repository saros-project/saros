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

import com.intellij.ui.AncestorListenerAdapter;

import javax.swing.JPanel;
import javax.swing.event.AncestorEvent;
import java.awt.BorderLayout;


/**
 * Saros main panel view. This is a JPanel and encapsulates the browser canvas.
 * This class is responsible for starting the SWT thread and managing the creation
 * and display of the browser.
 */
class SwtBrowserPanel extends JPanel {

    /**
     * Required for Linux, harmless for other OS.
     * <p>
     * <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=161911">SWT Component Not Displayed Bug</a>
     */
    static {
        System.setProperty("sun.awt.xembedserver", "true");
    }

    private SwtBrowserCanvas browserCanvas;

    private boolean initialized = false;

    SwtBrowserPanel() {
        super(new BorderLayout());
        /* As the browser gets disposed every time the tool window is hidden,
         * it has to be created again when it is re-shown.
         * The AncestorListener listens for that event. */
        addAncestorListener(new AncestorListenerAdapter() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                // this event is also triggered if the window was opened for the first time
                // we only want to start the shell for the times after that
                if (browserCanvas != null && initialized) {
                    browserCanvas.launchBrowser();
                }
            }
        });
        browserCanvas = new SwtBrowserCanvas(
            BrowserUtils.getUrlForClasspathFile("/html/saros-angular.html"));
    }

    /**
     * This method must be called *after* the enclosing frame has been made visible.
     * Otherwise the SWT AWT bridge will throw a {@link org.eclipse.swt.SWT#ERROR_INVALID_ARGUMENT}
     */
    void initialize() {
        assert browserCanvas != null;
        add(browserCanvas, BorderLayout.CENTER);
        browserCanvas.launchBrowser();
        initialized = true;
    }
}
