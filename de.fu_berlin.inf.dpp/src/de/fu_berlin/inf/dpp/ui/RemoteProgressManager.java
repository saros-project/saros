package de.fu_berlin.inf.dpp.ui;

import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.business.AbstractActivityReceiver;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.IActivityReceiver;
import de.fu_berlin.inf.dpp.activities.business.ProgressActivity;
import de.fu_berlin.inf.dpp.activities.business.ProgressActivity.ProgressAction;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.project.AbstractActivityProvider;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.IActivityProvider;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.util.StackTrace;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * The RemoteProgressManager is responsible for showing progress bars on the
 * machines of other users.
 */
@Component(module = "core")
public class RemoteProgressManager {

    private static final Logger log = Logger
        .getLogger(RemoteProgressManager.class);

    protected SarosSessionManager sessionManager;

    protected ISarosSession sarosSession;

    protected HashMap<String, RemoteProgress> progressDialogs = new HashMap<String, RemoteProgress>();

    /**
     * A remote progress represents a progress dialog being shown LOCALLY due to
     * ProgressActivities sent to the local user by a remote peer.
     */
    public class RemoteProgress {

        /**
         * The unique ID of this progress we are showing.
         */
        protected String progressID;

        /**
         * The user who requested a progress dialog to be shown.
         */
        protected User source;

        /**
         * A queue of incoming ProgressActivities which will be processed
         * locally to update the local Progress dialog.
         */
        protected LinkedBlockingQueue<ProgressActivity> activities = new LinkedBlockingQueue<ProgressActivity>();

        public RemoteProgress(String progressID, User source) {
            this.progressID = progressID;
            this.source = source;

            // Run async, so we can continue to receive messages over the
            // network...
            Utils.runSafeSWTAsync(log, new Runnable() {
                public void run() {
                    ProgressMonitorDialog dialog = new ProgressMonitorDialog(
                        EditorAPI.getAWorkbenchWindow().getShell());

                    try {
                        dialog.run(true, true, new IRunnableWithProgress() {
                            public void run(IProgressMonitor monitor) {
                                mainloop(SubMonitor.convert(monitor));
                            }
                        });
                    } catch (InvocationTargetException e) {
                        try {
                            throw e.getCause();
                        } catch (CancellationException c) {
                            log.info("Progress was cancelled by local user");
                        } catch (Throwable t) {
                            log.error("Internal Error: ", t);
                        }
                    } catch (InterruptedException e) {
                        log.error("Code not designed to be interruptable", e);
                    }
                    synchronized (RemoteProgress.this) {
                        activities = null; // Discard remaining activities
                    }
                }
            });
        }

        public synchronized void close() {
            if (activities == null)
                return;

            receive(new ProgressActivity(source, progressID, 0, 0, null,
                ProgressAction.DONE));
        }

        public synchronized void receive(ProgressActivity progressActivity) {
            if (!source.equals(progressActivity.getSource())) {
                log.warn("RemoteProgress with ID: " + progressID
                    + " is owned by buddy " + source
                    + " rejecting packet from other buddy: " + progressActivity);
                return;
            }
            if (activities == null) {
                log.debug("RemoteProgress with ID: " + progressID
                    + " has already been closed. Discarding activity: "
                    + progressActivity);
                return;
            }
            activities.add(progressActivity);
        }

        protected void mainloop(SubMonitor subMonitor) {
            int worked = 0;
            boolean firstTime = true;

            while (true) {
                ProgressActivity nextActivity;
                try {
                    nextActivity = activities.take();
                } catch (InterruptedException e) {
                    return;
                }

                switch (nextActivity.getAction()) {
                case UPDATE:
                    String taskName = nextActivity.getTaskName();
                    if (firstTime) {
                        subMonitor.beginTask(taskName,
                            nextActivity.getWorkTotal());
                        firstTime = false;
                    } else {
                        if (taskName != null)
                            subMonitor.subTask(taskName);

                        int newWorked = nextActivity.getWorkCurrent();
                        if (newWorked > worked) {
                            subMonitor.worked(newWorked - worked);
                            worked = newWorked;
                        }
                    }
                    break;
                case DONE:
                    subMonitor.done();
                    return;
                }
            }
        }
    }

    protected IActivityReceiver activityReceiver = new AbstractActivityReceiver() {
        @Override
        public void receive(ProgressActivity progressActivity) {

            String progressID = progressActivity.getProgressID();
            RemoteProgress progress = progressDialogs.get(progressID);
            if (progress == null) {
                progress = new RemoteProgress(progressID,
                    progressActivity.getSource());
                progressDialogs.put(progressID, progress);
            }
            progress.receive(progressActivity);
        }
    };

    protected IActivityProvider activityProvider = new AbstractActivityProvider() {
        @Override
        public void exec(IActivity activity) {
            activity.dispatch(activityReceiver);
        }
    };

    protected ISharedProjectListener sharedProjectListener = new AbstractSharedProjectListener() {
        @Override
        public void userLeft(User user) {
            for (RemoteProgress progress : progressDialogs.values()) {
                if (progress.source.equals(user))
                    progress.close();
            }
        }
    };

    protected ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {

        @Override
        public void sessionStarted(ISarosSession newSharedProject) {
            sarosSession = newSharedProject;

            newSharedProject.addActivityProvider(activityProvider);
            newSharedProject.addListener(sharedProjectListener);
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {

            oldSarosSession.removeActivityProvider(activityProvider);
            oldSarosSession.removeListener(sharedProjectListener);
            for (RemoteProgress progress : progressDialogs.values()) {
                progress.close();
            }
            progressDialogs.clear();

            sarosSession = null;
        }
    };

    public RemoteProgressManager(SarosSessionManager sessionManager) {
        this.sessionManager = sessionManager;
        this.sessionManager.addSarosSessionListener(sessionListener);
    }

    protected SimpleDateFormat format = new SimpleDateFormat("HHmmssSS");

    protected String getNextProgressID() {
        return format.format(new Date()) + Saros.RANDOM.nextLong();
    }

    /**
     * Returns a new IProgressMonitor which is displayed at the given remote
     * sites.
     * 
     * Usage:
     * 
     * - Call beginTask with the name of the Task to show to the user and the
     * total amount of work.
     * 
     * - Call worked to add amounts of work your task has finished (this will be
     * summed up and should not exceed totalWorked
     * 
     * - Call done as a last method to close the progress on the remote side.
     * 
     * Caution: This class does not check many invariants, but rather only sends
     * your commands to the remote party.
     * 
     */
    public IProgressMonitor createRemoteProgress(
        final ISarosSession sarosSession, final List<User> recipients) {
        return new IProgressMonitor() {

            protected String progressID = getNextProgressID();

            protected User localUser = sarosSession.getLocalUser();

            int worked = 0;

            int totalWorked = -1;

            public void beginTask(String name, int totalWorked) {
                this.totalWorked = totalWorked;
                sarosSession.sendActivity(recipients, new ProgressActivity(
                    localUser, progressID, 0, totalWorked, name,
                    ProgressAction.UPDATE));
            }

            public void done() {
                sarosSession.sendActivity(recipients, new ProgressActivity(
                    localUser, progressID, 0, 0, null, ProgressAction.DONE));
            }

            public void internalWorked(double work) {
                // do nothing
            }

            public boolean isCanceled() {
                // It would be cool to support communicating cancellation
                // to the originator
                return false;
            }

            public void setCanceled(boolean value) {
                throw new UnsupportedOperationException();
            }

            public void setTaskName(String name) {
                sarosSession.sendActivity(recipients, new ProgressActivity(
                    localUser, progressID, worked, totalWorked, name,
                    ProgressAction.UPDATE));
            }

            public void subTask(String name) {
                sarosSession.sendActivity(recipients, new ProgressActivity(
                    localUser, progressID, worked, totalWorked, name,
                    ProgressAction.UPDATE));
            }

            public void worked(int work) {
                worked += work;
                if (worked > totalWorked)
                    log.warn("Worked (" + worked
                        + ")is greater than totalWork (" + totalWorked
                        + "). Forgot to call beginTask?", new StackTrace());
                sarosSession.sendActivity(recipients, new ProgressActivity(
                    localUser, progressID, worked, totalWorked, null,
                    ProgressAction.UPDATE));
            }
        };
    }
}
