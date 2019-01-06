package de.fu_berlin.inf.dpp.ui.actions;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.concurrent.watchdog.ConsistencyWatchdogClient;
import de.fu_berlin.inf.dpp.concurrent.watchdog.IsInconsistentObservable;
import de.fu_berlin.inf.dpp.filesystem.EclipseReferencePointManager;
import de.fu_berlin.inf.dpp.monitoring.ProgressMonitorAdapterFactory;
import de.fu_berlin.inf.dpp.observables.ValueChangeListener;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.ISessionLifecycleListener;
import de.fu_berlin.inf.dpp.session.SessionEndReason;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.ui.views.SarosView;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.picocontainer.annotations.Inject;

/**
 * Please be aware of how we exploit the set***ImageDescriptor methods:
 *
 * <p><code>setImageDescriptor("foo")</code> with <code>setDisabledImageDescriptor(null)</code> will
 * display a gray scaled "foo" if the action is disabled, <br>
 * whereas <code>setImageDescriptor("foo")</code> with <code>setDisabledImageDescriptor("bar")
 * </code> will display "bar" if the action is disabled.
 */
@Component(module = "action")
public class ConsistencyAction extends Action implements Disposable {

  public static final String ACTION_ID = ConsistencyAction.class.getName();

  private static final Logger LOG = Logger.getLogger(ConsistencyAction.class);

  private static final int MIN_ALPHA_VALUE = 64;
  private static final int MAX_ALPHA_VALUE = 255;

  private static final int FADE_UPDATE_SPEED = 100; // ms
  private static final int FADE_SPEED_DELTA = 16;
  private static final int FADE_DOWN = -1;
  private static final int FADE_UP = 1;

  private static final ImageDescriptor IN_SYNC =
      ImageManager.getImageDescriptor("icons/etool16/in_sync.png"); // $NON-NLS-1$;

  private static final Image OUT_SYNC =
      ImageManager.getImage("icons/etool16/out_sync.png"); // $NON-NLS-1$;

  private static class MemoryImageDescriptor extends ImageDescriptor {

    private final ImageData data;

    public MemoryImageDescriptor(ImageData data) {
      this.data = data;
    }

    @Override
    public ImageData getImageData() {
      return data;
    }
  }

  private final ISessionLifecycleListener sessionLifecycleListener =
      new ISessionLifecycleListener() {
        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
          setSession(newSarosSession);
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession, SessionEndReason reason) {
          setSession(null);
        }
      };

  private final ValueChangeListener<Boolean> isConsistencyListener =
      new ValueChangeListener<Boolean>() {

        @Override
        public void setValue(Boolean newValue) {
          handleConsistencyChange(newValue);
        }
      };

  @Inject protected ISarosSessionManager sessionManager;

  @Inject protected IsInconsistentObservable inconsistentObservable;

  @Inject protected EclipseReferencePointManager eclipseReferencePointManager;

  private boolean isFading;

  private volatile ISarosSession sarosSession;

  public ConsistencyAction() {

    setId(ACTION_ID);
    setImageDescriptor(IN_SYNC);
    setToolTipText(Messages.ConsistencyAction_tooltip_no_inconsistency);

    SarosPluginContext.initComponent(this);

    sessionManager.addSessionLifecycleListener(sessionLifecycleListener);

    setSession(sessionManager.getSession());
  }

  private void setSession(ISarosSession newSession) {

    // TODO Why are we removing the listener and adding it again?

    if (sarosSession != null) {
      inconsistentObservable.remove(isConsistencyListener);
    }

    sarosSession = newSession;

    if (sarosSession != null) setDisabledImageDescriptor(IN_SYNC);
    else setDisabledImageDescriptor(null);

    if (sarosSession != null) {
      inconsistentObservable.addAndNotify(isConsistencyListener);
    } else {
      setEnabled(false);

      /*
       * make sure we reset the default "enabled image" otherwise the GUI
       * will grayscale the alpha scaled image and so it is possible that
       * nothing is displayed anymore
       */
      SWTUtils.runSafeSWTAsync(
          LOG,
          new Runnable() {
            @Override
            public void run() {
              setImageDescriptor(IN_SYNC);
            }
          });
    }
  }

  private void handleConsistencyChange(Boolean isInconsistent) {

    final ISarosSession currentSession = sarosSession;

    if (currentSession == null) return;

    final ConsistencyWatchdogClient watchdogClient =
        currentSession.getComponent(ConsistencyWatchdogClient.class);

    if (watchdogClient == null) {
      LOG.warn(
          "watchdog client component is not available, cannot perform inconsistency notification");
      return;
    }

    if (currentSession.isHost() && isInconsistent) {
      LOG.warn(
          "No inconsistency should ever be reported" //$NON-NLS-1$
              + " to the host"); //$NON-NLS-1$
      return;
    }

    LOG.debug(
        "Inconsistency indicator goes: " //$NON-NLS-1$
            + (isInconsistent ? "on" : "off")); // $NON-NLS-1$ //$NON-NLS-2$

    setEnabled(isInconsistent);

    if (!isInconsistent) {
      setToolTipText(Messages.ConsistencyAction_tooltip_no_inconsistency);
      return;
    }

    SWTUtils.runSafeSWTSync(
        LOG,
        new Runnable() {

          @Override
          public void run() {
            if (isFading) return;

            startFading(MAX_ALPHA_VALUE, FADE_DOWN);
          }
        });

    final Set<SPath> paths = new HashSet<SPath>(watchdogClient.getPathsWithWrongChecksums());

    SWTUtils.runSafeSWTAsync(
        LOG,
        new Runnable() {
          @Override
          public void run() {

            StringBuilder sb = new StringBuilder();

            for (SPath path : paths) {
              if (sb.length() > 0) sb.append(", ");

              sb.append(path.getFullPath().toOSString());
            }

            String files = sb.toString();

            // set tooltip
            setToolTipText(
                MessageFormat.format(
                    Messages.ConsistencyAction_tooltip_inconsistency_detected, files));

            // TODO Balloon is too aggressive at the moment, when
            // the host is slow in sending changes (for instance
            // when refactoring)

            // show balloon notification
            SarosView.showNotification(
                Messages.ConsistencyAction_title_inconsistency_deteced,
                MessageFormat.format(
                    Messages.ConsistencyAction_message_inconsistency_detected, files));
          }
        });
  }

  @Override
  public void run() {

    final ISarosSession currentSession = sarosSession;

    if (currentSession == null) return;

    final ConsistencyWatchdogClient watchdogClient =
        currentSession.getComponent(ConsistencyWatchdogClient.class);

    if (watchdogClient == null) {
      LOG.warn("watchdog client component is not available, cannot perform recovery");
      return;
    }

    LOG.debug("user activated CW recovery."); // $NON-NLS-1$

    Shell shell = SWTUtils.getShell();

    final Set<SPath> paths = new HashSet<SPath>(watchdogClient.getPathsWithWrongChecksums());

    String pluginID = Saros.PLUGIN_ID;

    MultiStatus multiStatus =
        new MultiStatus(
            pluginID,
            0,
            "The recovery process will perform changes to files and folders of the currently shared project(s). "
                + "The affected files and folders may be either modified, created, or deleted.\n\n"
                + "Press 'Details' for the affected files and folders.",
            null);

    for (SPath path : paths)
      multiStatus.add(
          new Status(
              IStatus.WARNING,
              pluginID,
              "project: "
                  + eclipseReferencePointManager.get(path.getReferencePoint()).getName()
                  + ", file:"
                  + path.getReferencePointRelativePath().toOSString()));

    class OkCancelErrorDialog extends ErrorDialog {
      public OkCancelErrorDialog(
          Shell parentShell, String dialogTitle, String message, IStatus status, int displayMask) {
        super(parentShell, dialogTitle, message, status, displayMask);
      }

      @Override
      protected void createButtonsForButtonBar(Composite parent) {
        // create OK, Cancel, and Details buttons
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, true);
        createDetailsButton(parent);
      }
    }

    OkCancelErrorDialog consistencyDialog =
        new OkCancelErrorDialog(
            shell,
            Messages.ConsistencyAction_confirm_dialog_title,
            null,
            multiStatus,
            IStatus.WARNING);

    if (consistencyDialog.open() != Window.OK) {
      return;
    }

    ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);

    try {
      dialog.run(
          true,
          true,
          new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor) throws InterruptedException {

              watchdogClient.runRecovery(ProgressMonitorAdapterFactory.convert(monitor));
            }
          });
    } catch (InvocationTargetException e) {
      LOG.error("Exception not expected here.", e); // $NON-NLS-1$
    } catch (InterruptedException e) {
      LOG.error("Exception not expected here.", e); // $NON-NLS-1$
    }
  }

  @Override
  public void dispose() {
    sessionManager.removeSessionLifecycleListener(sessionLifecycleListener);
  }

  /** Must be called within the SWT context. */
  private void startFading(final int startValue, final int direction) {
    Display display = SWTUtils.getDisplay();

    isFading = false;

    if (display.isDisposed() || !isEnabled()) return;

    isFading = true;

    display.timerExec(
        FADE_UPDATE_SPEED,
        new Runnable() {

          @Override
          public void run() {

            if (!isEnabled()) {
              setImageDescriptor(IN_SYNC);
              isFading = false;
              return;
            }

            int newValue = startValue + FADE_SPEED_DELTA * direction;
            int newDirection = direction;

            if (newValue > MAX_ALPHA_VALUE) {
              newValue = MAX_ALPHA_VALUE;
              newDirection = FADE_DOWN;
            } else if (newValue < MIN_ALPHA_VALUE) {
              newValue = MIN_ALPHA_VALUE;
              newDirection = FADE_UP;
            }

            setImageDescriptor(new MemoryImageDescriptor(modifyAlphaChannel(OUT_SYNC, newValue)));

            startFading(newValue, newDirection);
          }
        });
  }

  private static ImageData modifyAlphaChannel(Image image, int alpha) {

    if (alpha < 0) alpha = 0;

    if (alpha > 255) alpha = 255;

    ImageData data = image.getImageData();

    for (int x = 0; x < data.width; x++) {
      for (int y = 0; y < data.height; y++) {
        int a = data.getAlpha(x, y);

        // value depends on the image, must be determined empirically
        if (a <= MIN_ALPHA_VALUE) continue;

        data.setAlpha(x, y, alpha);
      }
    }

    return data;
  }
}
