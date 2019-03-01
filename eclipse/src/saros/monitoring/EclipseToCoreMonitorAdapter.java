package saros.monitoring;

import org.eclipse.core.runtime.ProgressMonitorWrapper;

public class EclipseToCoreMonitorAdapter extends ProgressMonitorWrapper
    implements saros.monitoring.IProgressMonitor {

  EclipseToCoreMonitorAdapter(org.eclipse.core.runtime.IProgressMonitor monitor) {
    super(monitor);
  }

  public org.eclipse.core.runtime.IProgressMonitor getDelegate() {
    return super.getWrappedProgressMonitor();
  }
}
