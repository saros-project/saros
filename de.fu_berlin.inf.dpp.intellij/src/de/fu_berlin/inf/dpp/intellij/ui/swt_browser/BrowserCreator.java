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

import de.fu_berlin.inf.dpp.ui.manager.HTMLUIManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * This class creates the SWT browser instance and the enclosing shell.
 */
class BrowserCreator {
    private Browser browser;

    BrowserCreator() {
    }

    /**
     * This methods creates a SWT shell and browser in the provided
     * AWT canvas.
     *
     * @param display   the SWT display
     * @param canvas    the AWT canvas to contain the SWT shell
     * @param startPage URL of the welcome page as string
     * @return this object
     */
    Browser createBrowser(Display display, final SwtBrowserCanvas canvas, String startPage) {
        Shell shell = SWT_AWT.new_Shell(display, canvas);
        browser = new Browser(shell, SWT.NONE);

        browser.setLocation(5, 5);
        /* Ideally the size of browser and shell gets set via a resize listener.
         * This does not work when the tool window is re-openend as no size
         * change event is fired. The if clause below sets the size for this case */
        if (canvas.getHeight() > 0 && canvas.getWidth() > 0) {
            shell.setSize(canvas.getWidth(), canvas.getHeight());
            browser.setSize(canvas.getWidth(), canvas.getHeight());
        }

        HTMLUIManager.create(browser);
        browser.setUrl(startPage);
        return browser;
    }
}

