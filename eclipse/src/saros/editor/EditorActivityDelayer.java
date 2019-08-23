package saros.editor;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Display;
import saros.activities.EditorActivity;
import saros.activities.IActivity;
import saros.activities.TextEditActivity;
import saros.activities.TextSelectionActivity;
import saros.activities.ViewportActivity;
import saros.session.AbstractActivityProducer;
import saros.session.ISarosSession;
import saros.ui.util.SWTUtils;
import saros.util.StackTrace;

/**
 * This class is responsible for delaying and discarding certain activities that are normally
 * generated frequently.
 */
// TODO Viewport activities are also fire to frequently, worst case is selecting text with mouse
// while scrolling up or down

final class EditorActivityDelayer extends AbstractActivityProducer {

  private static final int DELAY = 100; // ms

  private static final Logger log = Logger.getLogger(EditorActivityDelayer.class);

  private final Display display;

  private TextSelectionActivity currentSelectionActivity = null;

  EditorActivityDelayer() {
    display = SWTUtils.getDisplay();
  }

  void start(final ISarosSession session) {
    session.addActivityProducer(this);
  }

  void stop(final ISarosSession session) {
    session.removeActivityProducer(this);
  }

  void fireActivity(final ViewportActivity activity) {
    if (!checkThreadAccess(activity)) return;

    flushCurrentSelectionActivity();
    super.fireActivity(activity);
  }

  void fireActivity(final TextSelectionActivity activity) {
    if (!checkThreadAccess(activity)) return;

    if (currentSelectionActivity == null) {
      currentSelectionActivity = activity;
      display.timerExec(DELAY, this::flushCurrentSelectionActivity);
    } else if (currentSelectionActivity.getPath().equals(activity.getPath())) {
      currentSelectionActivity = activity;
    } else {
      flushCurrentSelectionActivity();
      currentSelectionActivity = activity;
      display.timerExec(DELAY, this::flushCurrentSelectionActivity);
    }
  }

  void fireActivity(final TextEditActivity activity) {
    if (!checkThreadAccess(activity)) return;

    flushCurrentSelectionActivity();
    super.fireActivity(activity);
  }

  void fireActivity(final EditorActivity activity) {
    if (!checkThreadAccess(activity)) return;

    flushCurrentSelectionActivity();
    super.fireActivity(activity);
  }

  private void flushCurrentSelectionActivity() {
    if (currentSelectionActivity == null) return;

    super.fireActivity(currentSelectionActivity);
    currentSelectionActivity = null;
  }

  private boolean checkThreadAccess(final IActivity activity) {
    if (Display.getCurrent() != null) return true;

    if (log.isDebugEnabled())
      log.error(
          "dropping activity " + activity + " due to invalid thread access", new StackTrace());
    else log.warn("dropping activity " + activity + " due to invalid thread access");

    return false;
  }
}
