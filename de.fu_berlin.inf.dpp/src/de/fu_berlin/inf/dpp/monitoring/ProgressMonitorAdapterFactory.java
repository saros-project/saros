package de.fu_berlin.inf.dpp.monitoring;

/**
 * Factory to create adapters from Eclipse
 * {@link org.eclipse.core.runtime.IProgressMonitor} to Saros Core
 * {@link IProgressMonitor} .
 */
public class ProgressMonitorAdapterFactory {

    /**
     * Convert an Eclipse ProgressMonitor to Saros Core ProgressMonitor
     * 
     * @param monitor
     *            of Eclipse
     * @return converted ProgressMonitor
     */
    public static IProgressMonitor convertTo(
        org.eclipse.core.runtime.IProgressMonitor monitor) {

        if (monitor == null)
            return null;

        return new EclipseProgressMonitorImpl(monitor);
    }

    /**
     * Converts a Saros Core IProgressMonitor to a Eclipse ProgressMonitor
     * 
     * @param monitor
     *            a Saros Core ProgressMonitor
     * @return the corresponding Eclipse
     *         {@linkplain org.eclipse.core.runtime.IProgressMonitor}
     * @deprecated This method can only handle
     *             {@link EclipseProgressMonitorImpl} implementations correctly.
     *             It is likely to produce a {@link ClassCastException} if
     *             invoked on any other implementations !
     */
    @Deprecated
    public static org.eclipse.core.runtime.IProgressMonitor convertBack(
        IProgressMonitor monitor) {

        if (monitor == null)
            return null;

        return ((EclipseProgressMonitorImpl) monitor)
            .getWrappedProgressMonitor();
    }

}
