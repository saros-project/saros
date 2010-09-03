package de.fu_berlin.inf.dpp.ui;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.novocode.naf.swt.custom.BalloonWindow;

import de.fu_berlin.inf.dpp.util.Util;

public class BalloonNotification {

    private static final Logger log = Logger
        .getLogger(BalloonNotification.class.getName());

    /**
     * Opens a notification window next to a control. The notification window
     * will show the given title as the title of the balloon, The given text as
     * the description.
     * 
     * The window will be hidden automatically after the value specified in the
     * timeout expires
     * 
     * @param pControl
     *            the control, next to where the widget will appear
     * @param pTitle
     *            the title of the balloon
     * @param pText
     *            the text to display as contents
     * @param pTimeout
     *            the timeout in milliseconds for automatically hidding the
     *            balloon
     */
    public static void showNotification(Control pControl, String pTitle,
        String pText, int pTimeout) {

        if (pControl != null && pControl.isDisposed()) {
            pControl = null;
        }

        Shell parentShell = (pControl != null ? pControl.getShell() : null);
        parentShell.forceActive();

        final BalloonWindow window = new BalloonWindow(parentShell, SWT.ON_TOP
            | SWT.TOOL | SWT.CLOSE | SWT.TITLE);

        window.setText(pTitle);

        // Adding the text to the contents. Pack() is required
        // so the size of the composite is recalculated, else
        // the contents won't show
        Composite c = window.getContents();
        c.setLayout(new FillLayout());
        Label l = new Label(c, SWT.NONE);
        l.setText(pText);
        c.pack(true);

        // Locate the balloon to the widget location
        if (pControl != null) {
            Point widgetLocation = pControl.toDisplay(new Point(0, 0));
            window.setLocation(widgetLocation);
        }

        // Runnable that will close the window after time has been expired
        window.getShell().getDisplay().timerExec(pTimeout,
            Util.wrapSafe(log, new Runnable() {

                public void run() {
                    window.getShell().forceActive();
                    window.close();
                }
            }));

        window.open();

    }
}
