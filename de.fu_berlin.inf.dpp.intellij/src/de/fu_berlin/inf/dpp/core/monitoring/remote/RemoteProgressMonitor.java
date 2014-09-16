package de.fu_berlin.inf.dpp.core.monitoring.remote;

import de.fu_berlin.inf.dpp.activities.ProgressActivity;
import de.fu_berlin.inf.dpp.activities.ProgressActivity.ProgressAction;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import de.fu_berlin.inf.dpp.monitoring.NullProgressMonitor;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.util.StackTrace;
import org.apache.log4j.Logger;

import java.util.Collection;

class RemoteProgressMonitor implements IProgressMonitor {

    private static final Logger LOG = Logger
        .getLogger(RemoteProgressMonitor.class);

    private final RemoteProgressManager rpm;
    private final User source;
    private final Collection<User> targets;
    private final IProgressMonitor monitor;

    private final String id;

    private int worked = 0;
    private int totalWorked = -1;

    RemoteProgressMonitor(final RemoteProgressManager rpm, final String id,
        final User source, final Collection<User> targets,
        IProgressMonitor monitor) {
        this.rpm = rpm;
        this.id = id;
        this.source = source;
        this.targets = targets;
        this.monitor = monitor == null ? new NullProgressMonitor() : monitor;
    }

    @Override
    public void beginTask(String name, int totalWorked) {
        // update local progress monitor
        monitor.beginTask(name, totalWorked);

        // report to remote monitor!
        this.totalWorked = totalWorked;
        createProgressActivityForUsers(0, totalWorked, name,
            ProgressAction.BEGINTASK);
    }

    @Override
    public void done() {
        monitor.done();
        createProgressActivityForUsers(0, 0, null, ProgressAction.DONE);
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
        monitor.setCanceled(value);
        createProgressActivityForUsers(worked, totalWorked, "Cancellation",
            ProgressAction.CANCEL);
    }

    @Override
    public void setTaskName(String name) {
        monitor.setTaskName(name);
        createProgressActivityForUsers(worked, totalWorked, name,
            ProgressAction.SETTASKNAME);
    }

    @Override
    public void subTask(String name) {
        monitor.subTask(name);
        createProgressActivityForUsers(worked, totalWorked, name,
            ProgressAction.SUBTASK);
    }

    @Override
    public void worked(int work) {
        monitor.worked(work);
        worked += work;

        if (worked > totalWorked) {
            LOG.warn(worked + " > " + totalWorked
                    + " (worked > totalworked) | maybe forget to call beginTask()",
                new StackTrace()
            );
        }

        createProgressActivityForUsers(worked, totalWorked, null,
            ProgressAction.UPDATE);
    }

    private void createProgressActivityForUsers(int workCurrent, int workTotal,
        String taskName, ProgressAction action) {

        for (final User target : targets)
            rpm.monitorUpdated(
                new ProgressActivity(source, target, id, workCurrent, workTotal,
                    taskName, action));
    }
}
