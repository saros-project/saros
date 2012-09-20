package de.fu_berlin.inf.dpp.util;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.ProgressMonitorWrapper;

public class ZipProgressMonitor extends ProgressMonitorWrapper implements
    ZipListener {

    private int lastWorked = 0;
    private int workRemaining = 0;
    private boolean useFilesSize = true;

    private int align = 1;

    public ZipProgressMonitor(IProgressMonitor monitor, int fileCount,
        boolean useFilesSize) {
        super(monitor == null ? new NullProgressMonitor() : monitor);

        this.useFilesSize = useFilesSize;

        workRemaining = fileCount;

        if (workRemaining <= 0)
            workRemaining = IProgressMonitor.UNKNOWN;

        if (useFilesSize)
            workRemaining = 100;

        beginTask("Compressing files...", workRemaining);
    }

    @Override
    public boolean update(String filename) {
        subTask("compressing file: " + filename);

        if (!useFilesSize && workRemaining != IProgressMonitor.UNKNOWN) {
            worked(1 - align);
            align = 0;
        }

        return isCanceled();
    }

    @Override
    public boolean update(long totalRead, long totalSize) {
        if (!useFilesSize || totalSize <= 0)
            return isCanceled();

        int worked = (int) ((totalRead * 100L) / totalSize);
        int workedDelta = worked - lastWorked;

        if (workedDelta > 0) {
            worked(workedDelta);
            lastWorked = worked;
        }
        return isCanceled();
    }
}
