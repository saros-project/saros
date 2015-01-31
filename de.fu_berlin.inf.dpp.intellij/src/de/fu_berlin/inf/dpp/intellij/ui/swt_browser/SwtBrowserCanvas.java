package de.fu_berlin.inf.dpp.intellij.ui.swt_browser;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.dpp.ui.view_parts.BrowserPage;
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

    private final BrowserPage startPage;
    private IJQueryBrowser browser;

    /**
     * @param startPage the BrowserPage object containing the page to be displayed
     */
    SwtBrowserCanvas(BrowserPage startPage) {
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
        browser.runOnDisposal(new Runnable() {
            @Override
            public void run() {
                removeComponentListener(resizeListener);
            }

        });
    }
}