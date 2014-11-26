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

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Display;

import java.awt.Canvas;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * This class is a AWT canvas and is responsible for launching the SWT browser.
 * It represents the AWT part of the AWT-SWT bridge.
 * The SWT part is done in the {@link BrowserCreator}.
 */
class SwtBrowserCanvas extends Canvas {

    private final String startPage;
    private Browser browser;

    /**
     * @param startPage the URL of the page to be displayed at startup
     */
    SwtBrowserCanvas(String startPage) {
        this.startPage = startPage;
    }

    /**
     * Creates and displays the SWT browser.
     * <p/>
     * This method must be called *after* the enclosing frame has been made visible.
     * Otherwise the SWT AWT bridge will throw a {@link org.eclipse.swt.SWT#ERROR_INVALID_ARGUMENT}
     */
    void launchBrowser() {
        final Display display = Display.getDefault();
        display.syncExec(new Runnable() {
            @Override
            public void run() {
                browser = new BrowserCreator()
                    .createBrowser(display, SwtBrowserCanvas.this, startPage);
                addResizeListener();
            }
        });
    }

    private void addResizeListener() {
        final ComponentAdapter resizeListener = new ComponentAdapter() {
            @Override
            public void componentResized(final ComponentEvent e) {
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        browser.setSize(e.getComponent().getWidth(),
                            e.getComponent().getHeight());
                    }
                });
            }
        };

        addComponentListener(resizeListener);
        browser.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                removeComponentListener(resizeListener);
            }
        });
    }
}
