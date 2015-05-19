package de.fu_berlin.inf.dpp.monitoring;

public class CoreToEclipseMonitorAdapter implements
    org.eclipse.core.runtime.IProgressMonitor,
    de.fu_berlin.inf.dpp.monitoring.IProgressMonitor {

    private final de.fu_berlin.inf.dpp.monitoring.IProgressMonitor monitor;

    public CoreToEclipseMonitorAdapter(
        de.fu_berlin.inf.dpp.monitoring.IProgressMonitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public void beginTask(String name, int totalWork) {
        monitor.beginTask(name, totalWork);
    }

    @Override
    public void done() {
        monitor.done();
    }

    @Override
    public void internalWorked(double work) {
        // TODO this might not work for specific monitors
        // NOP
    }

    @Override
    public boolean isCanceled() {
        return monitor.isCanceled();
    }

    @Override
    public void setCanceled(boolean value) {
        monitor.setCanceled(value);
    }

    @Override
    public void setTaskName(String name) {
        monitor.setTaskName(name);
    }

    @Override
    public void subTask(String name) {
        monitor.subTask(name);
    }

    @Override
    public void worked(int work) {
        monitor.worked(work);
    }
}
