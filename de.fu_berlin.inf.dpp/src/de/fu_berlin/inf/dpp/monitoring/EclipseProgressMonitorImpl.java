package de.fu_berlin.inf.dpp.monitoring;

import org.eclipse.core.runtime.ProgressMonitorWrapper;

public class EclipseProgressMonitorImpl extends ProgressMonitorWrapper
    implements IProgressMonitor {

    EclipseProgressMonitorImpl(org.eclipse.core.runtime.IProgressMonitor monitor) {
        super(monitor);
    }
}
