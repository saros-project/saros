package de.fu_berlin.inf.dpp.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.concurrent.watchdog.ConsistencyWatchdogClient;
import de.fu_berlin.inf.dpp.concurrent.watchdog.IsInconsistentObservable;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.ui.views.SarosView;
import de.fu_berlin.inf.dpp.util.Utils;
import de.fu_berlin.inf.dpp.util.ValueChangeListener;

/*
 * Please be aware of how we exploit the set***ImageDescriptor methods.
 * 
 * setImageDescriptor("foo") with setDisabledImageDescriptor(null)
 * 
 * will display a gray scaled "foo" if the action is disabled while
 * 
 * setImageDescriptor("foo") with setDisabledImageDescriptor("bar")
 * 
 * will display "bar" if the action is disabled
 */
@Component(module = "action")
public class ConsistencyAction extends Action {

    private static final int MIN_ALPHA_VALUE = 64;
    private static final int MAX_ALPHA_VALUE = 255;

    private static final int FADE_UPDATE_SPEED = 100; // ms
    private static final int FADE_SPEED_DELTA = 16;
    private static final int FADE_DOWN = -1;
    private static final int FADE_UP = 1;

    private static final ImageDescriptor IN_SYNC = ImageManager
        .getImageDescriptor("icons/etool16/in_sync.png"); //$NON-NLS-1$;

    private static final Image OUT_SYNC = ImageManager
        .getImage("icons/etool16/out_sync.png"); //$NON-NLS-1$;

    private static final Logger log = Logger.getLogger(ConsistencyAction.class);

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

    @Inject
    protected ISarosSessionManager sessionManager;

    @Inject
    protected ConsistencyWatchdogClient watchdogClient;

    @Inject
    protected IsInconsistentObservable inconsistentObservable;

    private boolean isFading;

    public ConsistencyAction() {

        setImageDescriptor(IN_SYNC);

        setToolTipText(Messages.ConsistencyAction_tooltip_no_inconsistency);

        SarosPluginContext.initComponent(this);

        sessionManager
            .addSarosSessionListener(new AbstractSarosSessionListener() {
                @Override
                public void sessionStarted(ISarosSession newSarosSession) {
                    setSharedProject(newSarosSession);
                }

                @Override
                public void sessionEnded(ISarosSession oldSarosSession) {
                    setSharedProject(null);
                }
            });

        setSharedProject(sessionManager.getSarosSession());
    }

    protected ISarosSession sarosSession;

    private void setSharedProject(ISarosSession newSharedProject) {

        // Unregister from previous project
        if (sarosSession != null) {
            inconsistentObservable.remove(isConsistencyListener);
        }

        sarosSession = newSharedProject;

        if (sarosSession != null)
            setDisabledImageDescriptor(IN_SYNC);
        else
            setDisabledImageDescriptor(null);

        // Register to new project
        if (sarosSession != null) {
            inconsistentObservable.addAndNotify(isConsistencyListener);
        } else {
            setEnabled(false);

            /*
             * make sure we reset the default "enabled image" otherwise the GUI
             * will grayscale the alpha scaled image and so it is possible that
             * nothing is displayed anymore
             */
            SWTUtils.runSafeSWTSync(log, new Runnable() {
                @Override
                public void run() {
                    setImageDescriptor(IN_SYNC);
                }
            });
        }
    }

    ValueChangeListener<Boolean> isConsistencyListener = new ValueChangeListener<Boolean>() {

        @Override
        public void setValue(Boolean newValue) {

            if (sarosSession.isHost() && newValue == true) {
                log.warn("No inconsistency should ever be reported" //$NON-NLS-1$
                    + " to the host"); //$NON-NLS-1$
                return;
            }
            log.debug("Inconsistency indicator goes: " //$NON-NLS-1$
                + (newValue ? "on" : "off")); //$NON-NLS-1$ //$NON-NLS-2$

            setEnabled(newValue);

            if (!newValue) {
                setToolTipText(Messages.ConsistencyAction_tooltip_no_inconsistency);
                return;
            }

            SWTUtils.runSafeSWTSync(log, new Runnable() {

                @Override
                public void run() {
                    if (isFading)
                        return;

                    startFading(MAX_ALPHA_VALUE, FADE_DOWN);
                }

            });

            final Set<SPath> paths = new HashSet<SPath>(
                watchdogClient.getPathsWithWrongChecksums());

            SWTUtils.runSafeSWTAsync(log, new Runnable() {
                @Override
                public void run() {

                    String files = Utils.toOSString(paths);

                    // set tooltip
                    setToolTipText(MessageFormat
                        .format(
                            Messages.ConsistencyAction_tooltip_inconsistency_detected,
                            files));

                    // TODO Balloon is too aggressive at the moment, when
                    // the host is slow in sending changes (for instance
                    // when refactoring)

                    // show balloon notification
                    SarosView
                        .showNotification(
                            Messages.ConsistencyAction_title_inconsistency_deteced,
                            MessageFormat
                                .format(
                                    Messages.ConsistencyAction_message_inconsistency_detected,
                                    files));
                }
            });
        }
    };

    @Override
    public void run() {
        SWTUtils.runSafeSWTAsync(log, new Runnable() {

            @Override
            public void run() {
                log.debug("user activated CW recovery."); //$NON-NLS-1$

                Shell dialogShell = SWTUtils.getShell();
                if (dialogShell == null)
                    dialogShell = new Shell();

                ProgressMonitorDialog dialog = new ProgressMonitorDialog(
                    dialogShell);
                try {
                    dialog.run(true, true, new IRunnableWithProgress() {
                        @Override
                        public void run(IProgressMonitor monitor)
                            throws InterruptedException {

                            SubMonitor progress = SubMonitor.convert(monitor);
                            progress
                                .beginTask(
                                    Messages.ConsistencyAction_progress_perform_recovery,
                                    100);
                            watchdogClient.runRecovery(progress.newChild(100));
                            monitor.done();
                        }
                    });
                } catch (InvocationTargetException e) {
                    log.error("Exception not expected here.", e); //$NON-NLS-1$
                } catch (InterruptedException e) {
                    log.error("Exception not expected here.", e); //$NON-NLS-1$
                }
            }
        });
    }

    /**
     * Must be called within the SWT context.
     */
    private void startFading(final int startValue, final int direction) {
        Display display = SWTUtils.getDisplay();

        isFading = false;

        if (display.isDisposed() || !isEnabled())
            return;

        isFading = true;

        display.timerExec(FADE_UPDATE_SPEED, new Runnable() {

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

                setImageDescriptor(new MemoryImageDescriptor(
                    modifyAlphaChannel(OUT_SYNC, newValue)));

                startFading(newValue, newDirection);
            }

        });
    }

    private static ImageData modifyAlphaChannel(Image image, int alpha) {

        if (alpha < 0)
            alpha = 0;

        if (alpha > 255)
            alpha = 255;

        ImageData data = image.getImageData();

        for (int x = 0; x < data.width; x++) {
            for (int y = 0; y < data.height; y++) {
                int a = data.getAlpha(x, y);

                // value depends on the image, must be determined empirically
                if (a <= MIN_ALPHA_VALUE)
                    continue;

                data.setAlpha(x, y, alpha);
            }
        }

        return data;
    }
}
