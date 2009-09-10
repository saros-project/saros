package de.fu_berlin.inf.dpp.ui.actions;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.concurrent.watchdog.ConsistencyWatchdogClient;
import de.fu_berlin.inf.dpp.project.AbstractSessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.util.Util;
import de.fu_berlin.inf.dpp.util.ValueChangeListener;

@Component(module = "action")
public class ConsistencyAction extends Action {

    private static Logger log = Logger.getLogger(ConsistencyAction.class);

    protected SessionManager sessionManager;

    protected ConsistencyWatchdogClient watchdogClient;

    public ConsistencyAction(ConsistencyWatchdogClient watchdogClient,
        SessionManager sessionManager) {
        setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
            .getImageDescriptor(ISharedImages.IMG_OBJS_WARN_TSK));
        setToolTipText("No inconsistencies");
        this.watchdogClient = watchdogClient;
        this.sessionManager = sessionManager;

        sessionManager.addSessionListener(new AbstractSessionListener() {
            @Override
            public void sessionStarted(ISharedProject session) {
                setSharedProject(session);
            }

            @Override
            public void sessionEnded(ISharedProject session) {
                setSharedProject(null);
            }
        });

        setSharedProject(sessionManager.getSharedProject());

    }

    protected ISharedProject sharedProject;

    private void setSharedProject(ISharedProject newSharedProject) {

        // Unregister from previous project
        if (sharedProject != null) {
            watchdogClient.getConsistencyToResolve().remove(
                isConsistencyListener);
        }

        sharedProject = newSharedProject;

        // Register to new project
        if (sharedProject != null) {
            watchdogClient.getConsistencyToResolve().addAndNotify(
                isConsistencyListener);
        } else {
            setEnabled(false);
        }
    }

    ValueChangeListener<Boolean> isConsistencyListener = new ValueChangeListener<Boolean>() {

        public void setValue(Boolean newValue) {

            if (sharedProject.isHost() && newValue == true) {
                log.warn("No inconsistency should ever be reported"
                    + " to the host");
                return;
            }
            log.debug("Inconsistency indicator goes: "
                + (newValue ? "on" : "off"));
            setEnabled(newValue);

            if (newValue) {
                final Set<IPath> paths = new HashSet<IPath>(watchdogClient
                    .getPathsWithWrongChecksums());

                Util.runSafeSWTAsync(log, new Runnable() {
                    public void run() {

                        // set tooltip
                        setToolTipText("Inconsistency Detected in file/s "
                            + Util.toOSString(paths));

                        // TODO Balloon is too aggressive at the moment, when
                        // the host is slow in sending changes (for instance
                        // when refactoring)

                        // show balloon notification
                        /*
                         * BalloonNotification.showNotification(
                         * ((ToolBarManager) toolBar).getControl(),
                         * "Inconsistency Detected!",
                         * "Inconsistencies detected in: " +
                         * pathsOfInconsistencies, 5000);
                         */
                    }
                });

            } else {
                setToolTipText("No inconsistencies");
            }
        }

    };

    @Override
    public void run() {
        log.debug("User activated CW recovery.");
        Util.runSafeAsync(log, new Runnable() {
            public void run() {
                watchdogClient.runRecovery();
            }
        });
    }

}
