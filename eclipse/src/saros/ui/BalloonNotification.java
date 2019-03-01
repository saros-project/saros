package saros.ui;

import com.novocode.naf.swt.custom.BalloonWindow;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import saros.util.ThreadUtils;

public class BalloonNotification {

  private static final Logger log = Logger.getLogger(BalloonNotification.class.getName());

  private static Color BLACK = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
  private static Color WHITE = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);

  private static List<BalloonWindow> windows = new ArrayList<BalloonWindow>();

  /**
   * Close all currently active notification windows
   *
   * <p>It would probably suffice to only remember the last notification window, because currently
   * all windows are displayed in the same position, overlapping each other. But if we ever manage
   * to (FIXME) display balloon notifications in different spots, they might not overlap and
   * should/remain.
   */
  public static void removeAllActiveNotifications() {
    for (BalloonWindow window : windows) {
      window.close();
    }
    windows.clear();
  }

  /**
   * Opens a notification window next to a control. The notification window will show the given
   * title as the title of the balloon, The given text as the description.
   *
   * <p>The window will be hidden automatically after the value specified in the timeout expires
   *
   * <p>TODO wrap the contents so the balloon notification does not expand across two screens for a
   * long text. OR even better: do not show long notifications. users tend to ignore them anyway!
   *
   * @param control the control, next to where the widget will appear
   * @param title the title of the balloon
   * @param text the text to display as contents
   */
  public static void showNotification(Control control, String title, String text) {

    if (control != null && control.isDisposed()) {
      control = null;
    }

    /*
     * show message at least 8 secs, but show it longer for longer messages
     * Referenced by wikipedia, a user can read 2,5 words per second so we
     * approximate 400ms per word
     */
    int timeout = Math.max(8000, text.split("\\s").length * 400);

    // close all previous balloon notifications like it is done in
    // windows to prevent overlapping of multiple balloons...
    BalloonNotification.removeAllActiveNotifications();

    final BalloonWindow window =
        new BalloonWindow(
            control != null ? control.getShell() : null, SWT.NO_FOCUS | SWT.TOOL | SWT.TITLE);

    windows.add(window);
    /*
     * Note: if you add SWT.CLOSE to the style of the BalloonWindow, it will
     * only be closed when directly clicking on the close icon (x) and
     * therefore break user expectations. FIXME: find out a way to display
     * the closing X AND make the bubble close on any click anywhere on it.
     */

    window.setText(title);

    /*
     * Adding the text to the contents. Pack() is required so the size of
     * the composite is recalculated, else the contents won't show
     */
    Composite content = window.getContents();
    content.setLayout(new FillLayout());

    Label message = new Label(content, SWT.NONE);
    message.setText(text);
    content.pack(true);

    message.setBackground(WHITE);
    message.setForeground(BLACK);

    // make window close when clicking on balloon text, too
    window.addSelectionControl(message);

    // Locate the balloon to the widget location
    if (control != null) {
      Point widgetLocation = control.toDisplay(new Point(0, 0));
      window.setLocation(widgetLocation);
    }

    // Runnable that will close the window after time has been expired
    final Runnable closeWindow =
        ThreadUtils.wrapSafe(
            log,
            new Runnable() {

              @Override
              public void run() {
                final Shell shell = window.getShell();
                if (shell.isDisposed()) return;

                window.close();
              }
            });

    window.getShell().getDisplay().timerExec(timeout, closeWindow);

    Display display = control != null ? control.getDisplay() : Display.getCurrent();

    Control lastControlWithFocus = display != null ? display.getFocusControl() : null;

    window.open();

    if (lastControlWithFocus != null) lastControlWithFocus.setFocus();
  }
}
