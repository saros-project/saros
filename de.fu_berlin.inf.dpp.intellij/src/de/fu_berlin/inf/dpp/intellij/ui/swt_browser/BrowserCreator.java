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

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import java.awt.Canvas;

/**
 * This class creates the SWT browser instance, the enclosing shell and a rudimentary
 * address bar.
 */
class BrowserCreator {

    private Browser browser;

    /**
     * This methods creates a SWT shell and browser in the provided
     * AWT canvas. At the moment the browser has an address bar for
     * testing purposes.
     *
     * @param display   the SWT display
     * @param canvas    the AWT canvas to contain the SWT shell
     * @param startPage URL of the welcome page as string
     * @return this object
     */
    Browser createBrowser(Display display, Canvas canvas, String startPage) {
        Shell shell = SWT_AWT.new_Shell(display, canvas);
        browser = new Browser(shell, SWT.NONE);

        browser.setLocation(5, 30);
        /* Ideally the size of browser and shell gets set via a resize listener.
         * This does not worked when the tool window is re-openend as no size
         * change event is fired. The if clause below sets the size for this case */
        if (canvas.getHeight() > 0 && canvas.getWidth() > 0) {
            shell.setSize(canvas.getWidth(), canvas.getHeight());
            browser.setSize(canvas.getWidth(), canvas.getHeight());
        }
        createAddressBar(shell);

        browser.setUrl(startPage);
        return browser;
    }

    private void createAddressBar(Shell shell) {
        final Text text = new Text(shell, SWT.BORDER);
        text.setBounds(5, 5, 400, 25);
        text.addListener(SWT.DefaultSelection, new Listener() {
            @Override
            public void handleEvent(Event e) {
                browser.setUrl(text.getText());
            }
        });

        Button reloadButton = new Button(shell, SWT.NONE);
        reloadButton.setText("Refresh");
        reloadButton.setBounds(420, 5, 65, 25);
        reloadButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                browser.refresh();
            }
        });
    }

}

