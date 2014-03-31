package de.fu_berlin.inf.dpp.ui;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.activities.business.AbstractActivityReceiver;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.IActivityReceiver;
import de.fu_berlin.inf.dpp.activities.business.ProgressActivity;
import de.fu_berlin.inf.dpp.activities.business.ProgressActivity.ProgressAction;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.AbstractActivityProvider;
import de.fu_berlin.inf.dpp.session.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISharedProjectListener;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.util.StackTrace;

/**
 * The RemoteProgressManager is responsible for showing progress bars on the
 * machines of other users.
 */
@Component(module = "core")
public class RemoteProgressManager {

    private static final Logger log = Logger
        .getLogger(RemoteProgressManager.class);

    protected ISarosSessionManager sessionManager;

    protected ISarosSession sarosSession;

    protected HashMap<String, RemoteProgress> progressDialogs = new HashMap<String, RemoteProgress>();

    /**
     * A remote progress represents a progress dialog being shown LOCALLY due to
     * ProgressActivities sent to the local user by a remote peer.
     */
    public static class RemoteProgress {

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
            // network. Run as a job, so that it can be run in background
            // for remote hosts
            // FIXME NICKNAME !!!
            Job job = new Job("Observing remote progress for " + source) {
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    try {
                        mainloop(SubMonitor.convert(monitor));
                    } catch (Exception e) {
                        log.error("", e);
                        return Status.CANCEL_STATUS;
                    }
                    return Status.OK_STATUS;
                }
            };
            job.setPriority(Job.SHORT);
            job.setUser(true);
            job.schedule();
        }

        public synchronized void close() {
            if (activities == null)
                return;

            /**
             * This Activity is just used as a PoisonPill for the ActivityLoop
             * of the ProgressMonitor (identified by the ProgressAction.DONE)
             * and therefore most values don't have to be set correctly as this
             * Activity will never be sent over the Network.
             */

            receive(new ProgressActivity(source, source, progressID, 0, 0,
                null, ProgressAction.DONE));
        }

        public synchronized void receive(ProgressActivity progressActivity) {
            if (!source.equals(progressActivity.getSource())) {
                log.warn("RemoteProgress with ID: " + progressID
                    + " is owned by user " + source
                    + " rejecting packet from other user: " + progressActivity);
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
                ProgressActivity activity;
                try {
                    if (subMonitor.isCanceled()) {
                        return;
                    }
                    activity = activities.poll(1000, TimeUnit.MILLISECONDS);
                    if (activity == null) {
                        continue;
                    }
                } catch (InterruptedException e) {
                    return;
                }
                String taskName = activity.getTaskName();
                int newWorked;
                log.debug("RemoteProgressActivity: " + taskName + " / "
                    + activity.getAction());

                switch (activity.getAction()) {
                case BEGINTASK:
                    subMonitor.beginTask(taskName, activity.getWorkTotal());
                    break;
                case SETTASKNAME:
                    subMonitor.setTaskName(taskName);
                    break;
                case SUBTASK:
                    if (taskName != null)
                        subMonitor.subTask(taskName);
                    newWorked = activity.getWorkCurrent();
                    if (newWorked > worked) {
                        subMonitor.worked(newWorked - worked);
                        worked = newWorked;
                    }
                    break;
                case UPDATE:
                    if (firstTime) {
                        subMonitor.beginTask(taskName, activity.getWorkTotal());
                        firstTime = false;
                    } else {
                        if (taskName != null)
                            subMonitor.subTask(taskName);

                        newWorked = activity.getWorkCurrent();
                        if (newWorked > worked) {
                            subMonitor.worked(newWorked - worked);
                            worked = newWorked;
                        }
                    }
                    break;
                case DONE:
                    subMonitor.done();
                    return;
                case CANCEL:
                    log.info("Progress was cancelled by remote user");
                    subMonitor.setCanceled(true);
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

    protected AbstractActivityProvider activityProvider = new AbstractActivityProvider() {
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

    public RemoteProgressManager(ISarosSessionManager sessionManager) {
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

            @Override
            public void beginTask(String name, int totalWorked) {
                this.totalWorked = totalWorked;
                createProgressActivityForUsers(localUser, recipients,
                    progressID, 0, totalWorked, name, ProgressAction.UPDATE);
            }

            @Override
            public void done() {
                createProgressActivityForUsers(localUser, recipients,
                    progressID, 0, 0, null, ProgressAction.DONE);
            }

            @Override
            public void internalWorked(double work) {
                // do nothing
            }

            @Override
            public boolean isCanceled() {
                // It would be cool to support communicating cancellation
                // to the originator
                return false;
            }

            @Override
            public void setCanceled(boolean value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void setTaskName(String name) {
                createProgressActivityForUsers(localUser, recipients,
                    progressID, worked, totalWorked, name,
                    ProgressAction.UPDATE);
            }

            @Override
            public void subTask(String name) {
                createProgressActivityForUsers(localUser, recipients,
                    progressID, worked, totalWorked, name,
                    ProgressAction.UPDATE);
            }

            @Override
            public void worked(int work) {
                worked += work;
                if (worked > totalWorked)
                    log.warn(
                        MessageFormat
                            .format(
                                "Worked ({0})is greater than totalWork ({1}). Forgot to call beginTask?",
                                worked, totalWorked), new StackTrace());
                createProgressActivityForUsers(localUser, recipients,
                    progressID, worked, totalWorked, null,
                    ProgressAction.UPDATE);
            }

            private void createProgressActivityForUsers(User source,
                List<User> recipients, String progressID, int workCurrent,
                int workTotal, String taskName, ProgressAction action) {
                for (User target : recipients) {
                    activityProvider.fireActivity(new ProgressActivity(source,
                        target, progressID, workCurrent, workTotal, taskName,
                        action));
                }

            }
        };
    }

    /**
     * This wraps the given progress monitor so that any progress reported via
     * the original monitor is reported to the listed remote hosts too.
     * 
     * Background: Sometimes we run a process locally and need to show the user
     * progress, so he/she can abort the process. But we also need to report the
     * progress to remote users.
     * 
     * @param session
     * @param target
     * @param monitor
     * @return
     */
    public IProgressMonitor mirrorLocalProgressMonitorToRemote(
        final ISarosSession session, final User target,
        final IProgressMonitor monitor) {

        return new IProgressMonitor() {
            protected String progressID = getNextProgressID();
            protected User localUser = sarosSession.getLocalUser();
            int worked = 0;
            int totalWorked = -1;

            @Override
            public void beginTask(String name, int totalWorked) {
                // update local progress monitor
                monitor.beginTask(name, totalWorked);

                // report to remote monitor!
                this.totalWorked = totalWorked;
                activityProvider.fireActivity(new ProgressActivity(localUser,
                    target, progressID, 0, totalWorked, name,
                    ProgressAction.BEGINTASK));
            }

            @Override
            public void done() {
                monitor.done();
                activityProvider.fireActivity(new ProgressActivity(localUser,
                    target, progressID, 0, 0, null, ProgressAction.DONE));
            }

            /**
             * FIXME: This is not yet propagated remotely
             */
            @Override
            public void internalWorked(double work) {
                monitor.internalWorked(work);
            }

            @Override
            public boolean isCanceled() {
                return monitor.isCanceled();
            }

            /**
             * FIXME: This is not yet propagated remotely
             */
            @Override
            public void setCanceled(boolean value) {
                // waldmann: yep this is a TODO
                activityProvider.fireActivity(new ProgressActivity(localUser,
                    target, progressID, worked, totalWorked, "Cancellation",
                    ProgressAction.CANCEL));
                monitor.setCanceled(value);
            }

            @Override
            public void setTaskName(String name) {
                monitor.setTaskName(name);
                activityProvider.fireActivity(new ProgressActivity(localUser,
                    target, progressID, worked, totalWorked, name,
                    ProgressAction.SETTASKNAME));
            }

            @Override
            public void subTask(String name) {
                monitor.subTask(name);
                activityProvider.fireActivity(new ProgressActivity(localUser,
                    target, progressID, worked, totalWorked, name,
                    ProgressAction.SUBTASK));
            }

            @Override
            public void worked(int work) {
                monitor.worked(work);
                worked += work;
                if (worked > totalWorked)
                    log.warn(
                        MessageFormat
                            .format(
                                "Worked ({0})is greater than totalWork ({1}). Forgot to call beginTask?",
                                worked, totalWorked), new StackTrace());
                activityProvider.fireActivity(new ProgressActivity(localUser,
                    target, progressID, worked, totalWorked, null,
                    ProgressAction.UPDATE));
            }
        };
    }
}
