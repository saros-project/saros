package de.fu_berlin.inf.dpp.monitoring.remote;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import de.fu_berlin.inf.dpp.activities.ProgressActivity;
import de.fu_berlin.inf.dpp.activities.ProgressActivity.ProgressAction;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.ModelFormatUtils;

/**
 * A remote progress represents a progress dialog being shown locally which is
 * updated via {@link ProgressActivity activities} sent by a remote user.
 * 
 */
final class RemoteProgress {

    private static final Logger LOG = Logger.getLogger(RemoteProgress.class);

    /**
     * The unique ID of this progress.
     */
    private final String id;

    /**
     * The user who requested a progress dialog to be shown.
     */
    private final User source;

    private final RemoteProgressManager rpm;

    private boolean running;

    private boolean started;

    /**
     * A queue of incoming ProgressActivities which will be processed locally to
     * update the local Progress dialog.
     */
    private LinkedBlockingQueue<ProgressActivity> activities = new LinkedBlockingQueue<ProgressActivity>();

    RemoteProgress(final RemoteProgressManager rpm, final String id,
        final User source) {
        this.rpm = rpm;
        this.id = id;
        this.source = source;
    }

    User getSource() {
        return source;
    }

    synchronized void start() {
        if (started)
            return;

        started = true;

        final Job job = new Job(ModelFormatUtils.format(
            Messages.RemoteProgress_observing_progress_for, source)) {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    mainloop(monitor);
                    return Status.OK_STATUS;
                } catch (Exception e) {
                    LOG.error(e);
                    return Status.CANCEL_STATUS;
                } finally {
                    rpm.removeProgress(id);
                }
            }
        };

        job.setPriority(Job.SHORT);
        job.setUser(true);
        job.schedule();
        running = true;
    }

    synchronized void close() {
        if (!running)
            return;

        running = true;

        /**
         * This Activity is just used as a PoisonPill for the ActivityLoop of
         * the ProgressMonitor (identified by the ProgressAction.DONE) and
         * therefore most values don't have to be set correctly as this Activity
         * will never be sent over the Network.
         */

        execute(new ProgressActivity(source, source, id, 0, 0, null,
            ProgressAction.DONE));
    }

    synchronized void execute(ProgressActivity activity) {
        if (!source.equals(activity.getSource())) {
            LOG.warn("RemoteProgress with ID: " + id + " is owned by user "
                + source + " rejecting activity from other user: " + activity);
            return;
        }

        if (!running) {
            LOG.debug("RemoteProgress with ID: " + id
                + " has already been closed. Discarding activity: " + activity);
            return;
        }

        activities.add(activity);
    }

    private void mainloop(final IProgressMonitor monitor) {
        int worked = 0;
        boolean firstTime = true;

        update: while (true) {

            final ProgressActivity activity;

            try {
                if (monitor.isCanceled())
                    break update;

                // poll so this monitor can be closed locally
                activity = activities.poll(1000, TimeUnit.MILLISECONDS);

                if (activity == null)
                    continue update;

            } catch (InterruptedException e) {
                return;
            }

            final String taskName = activity.getTaskName();

            int newWorked;

            if (LOG.isTraceEnabled())
                LOG.trace("executing progress activity: " + activity);

            switch (activity.getAction()) {
            case BEGINTASK:
                monitor.beginTask(taskName, activity.getWorkTotal());
                continue update;
            case SETTASKNAME:
                monitor.setTaskName(taskName);
                continue update;
            case SUBTASK:
                if (taskName != null)
                    monitor.subTask(taskName);
                newWorked = activity.getWorkCurrent();
                if (newWorked > worked) {
                    monitor.worked(newWorked - worked);
                    worked = newWorked;
                }
                continue update;
            case UPDATE:
                if (firstTime) {
                    monitor.beginTask(taskName, activity.getWorkTotal());
                    firstTime = false;
                } else {
                    if (taskName != null)
                        monitor.subTask(taskName);

                    newWorked = activity.getWorkCurrent();
                    if (newWorked > worked) {
                        monitor.worked(newWorked - worked);
                        worked = newWorked;
                    }
                }
                continue update;
            case DONE:
                monitor.done();
                break update;
            case CANCEL:
                LOG.debug("progress was cancelled by remote user");
                monitor.setCanceled(true);
                break update;
            }
        }
    }

    @Override
    public int hashCode() {
        return ((id == null) ? 0 : id.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof RemoteProgress))
            return false;

        return ObjectUtils.equals(id, ((RemoteProgress) obj).id)
            && ObjectUtils.equals(source, ((RemoteProgress) obj).source);
    }
}
