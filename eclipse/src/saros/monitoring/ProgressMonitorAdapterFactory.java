package saros.monitoring;

/**
 * Factory to create adapters from Eclipse {@link org.eclipse.core.runtime.IProgressMonitor} to
 * Saros Core {@link saros.monitoring.IProgressMonitor} and vice versa.
 */
public class ProgressMonitorAdapterFactory {

  /**
   * Convert an Eclipse ProgressMonitor to Saros Core ProgressMonitor
   *
   * @param monitor an Eclipse ProgressMonitor
   * @return converted ProgressMonitor
   */
  public static saros.monitoring.IProgressMonitor convert(
      org.eclipse.core.runtime.IProgressMonitor monitor) {

    if (monitor == null) return null;

    if (monitor instanceof saros.monitoring.IProgressMonitor)
      return (saros.monitoring.IProgressMonitor) monitor;

    return new EclipseToCoreMonitorAdapter(monitor);
  }

  /**
   * Converts a Saros Core ProgressMonitor to a Eclipse ProgressMonitor
   *
   * @param monitor a Saros Core ProgressMonitor
   * @return the corresponding Eclipse {@linkplain org.eclipse.core.runtime.IProgressMonitor}
   */
  public static org.eclipse.core.runtime.IProgressMonitor convert(
      saros.monitoring.IProgressMonitor monitor) {

    if (monitor == null) return null;

    if (monitor instanceof org.eclipse.core.runtime.IProgressMonitor)
      return (org.eclipse.core.runtime.IProgressMonitor) monitor;

    return new CoreToEclipseMonitorAdapter(monitor);
  }
}
