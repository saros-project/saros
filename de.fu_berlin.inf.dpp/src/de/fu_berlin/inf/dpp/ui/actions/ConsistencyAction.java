package de.fu_berlin.inf.dpp.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.concurrent.watchdog.ConsistencyWatchdogClient;
import de.fu_berlin.inf.dpp.concurrent.watchdog.IsInconsistentObservable;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.ui.views.SarosView;
import de.fu_berlin.inf.dpp.util.Utils;
import de.fu_berlin.inf.dpp.util.ValueChangeListener;

@Component(module = "action")
public class ConsistencyAction extends Action {

    private static Logger log = Logger.getLogger(ConsistencyAction.class);

    @Inject
    protected SarosSessionManager sessionManager;

    @Inject
    protected ConsistencyWatchdogClient watchdogClient;

    @Inject
    protected IsInconsistentObservable inconsistentObservable;

    public ConsistencyAction() {

        setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
            .getImageDescriptor(ISharedImages.IMG_OBJS_WARN_TSK));
        setToolTipText("No inconsistencies");

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

        // Register to new project
        if (sarosSession != null) {
            inconsistentObservable.addAndNotify(isConsistencyListener);
        } else {
            setEnabled(false);
        }
    }

    ValueChangeListener<Boolean> isConsistencyListener = new ValueChangeListener<Boolean>() {

        public void setValue(Boolean newValue) {

            if (sarosSession.isHost() && newValue == true) {
                log.warn("No inconsistency should ever be reported"
                    + " to the host");
                return;
            }
            log.debug("Inconsistency indicator goes: "
                + (newValue ? "on" : "off"));
            setEnabled(newValue);

            if (newValue) {
                final Set<SPath> paths = new HashSet<SPath>(
                    watchdogClient.getPathsWithWrongChecksums());

                Utils.runSafeSWTAsync(log, new Runnable() {
                    public void run() {

                        String files = Utils.toOSString(paths);

                        // set tooltip
                        setToolTipText("Inconsistency Detected in file(s): "
                            + files);

                        // TODO Balloon is too aggressive at the moment, when
                        // the host is slow in sending changes (for instance
                        // when refactoring)

                        // show balloon notification
                        SarosView
                            .showNotification(
                                "Inconsistencies detected",
                                "These files have become unsynchronised with the host:\n"
                                    + files
                                    + "\n\nPress the inconsistency recovery button to synchronise your project."
                                    + " \nYou may wish to backup those file(s) in case important changes are overwritten.");
                    }
                });

            } else {
                setToolTipText("No inconsistencies");
            }
        }

    };

    @Override
    public void run() {
        Utils.runSafeSWTAsync(log, new Runnable() {

            public void run() {
                log.debug("Buddy activated CW recovery.");

                Shell dialogShell = EditorAPI.getShell();
                if (dialogShell == null)
                    dialogShell = new Shell();

                ProgressMonitorDialog dialog = new ProgressMonitorDialog(
                    dialogShell);
                try {
                    dialog.run(true, true, new IRunnableWithProgress() {
                        public void run(IProgressMonitor monitor)
                            throws InterruptedException {

                            SubMonitor progress = SubMonitor.convert(monitor);
                            progress.beginTask("Performing recovery...", 100);
                            watchdogClient.runRecovery(progress.newChild(100));
                            monitor.done();
                        }
                    });
                } catch (InvocationTargetException e) {
                    log.error("Exception not expected here.", e);
                } catch (InterruptedException e) {
                    log.error("Exception not expected here.", e);
                }
            }
        });
    }
}
