package de.fu_berlin.inf.dpp.invitation;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.util.CoreUtils;

// TODO java doc
public class CreateArchiveTask implements IWorkspaceRunnable {

    private static final int BUFFER_SIZE = 32 * 1024;

    private static final Logger LOG = Logger.getLogger(CreateArchiveTask.class);

    private final File archive;
    private final List<IFile> files;
    private final List<String> alias;
    private final IProgressMonitor monitor;

    public CreateArchiveTask(final File archive, final List<IFile> files,
        final List<String> alias, final IProgressMonitor monitor) {
        this.archive = archive;
        this.files = files;
        this.alias = alias;
        this.monitor = monitor;
    }

    @Override
    public void run(IProgressMonitor monitor) throws CoreException {
        if (this.monitor != null)
            monitor = this.monitor;

        assert files.size() == alias.size();

        long totalSize = 0L;

        for (IFile file : files) {
            try {
                totalSize += file.getSize();
            } catch (IOException e) {
                LOG.warn("unable to retrieve file size for file: "
                    + file.getFullPath().toString(), e);
            }
        }

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        final Iterator<IFile> fileIt = files.iterator();
        final Iterator<String> aliasIt = alias == null ? null : alias
            .iterator();

        long totalRead = 0L;

        boolean cleanup = true;

        byte[] buffer = new byte[BUFFER_SIZE];

        ZipOutputStream zipStream = null;

        final SubMonitor progress = SubMonitor.convert(monitor,
            "Compressing files...", 100 /* percent */);

        try {
            zipStream = new ZipOutputStream(new BufferedOutputStream(
                new FileOutputStream(archive), BUFFER_SIZE));

            while (fileIt.hasNext()) {

                IFile file = fileIt.next();

                String entryName = null;

                final String originalEntryName = file.getFullPath().toString();

                if (aliasIt != null && aliasIt.hasNext())
                    entryName = aliasIt.next();

                if (entryName == null)
                    entryName = originalEntryName;

                if (LOG.isTraceEnabled())
                    LOG.trace("compressing file: " + originalEntryName);

                progress.subTask("compressing file: " + originalEntryName);

                zipStream.putNextEntry(new ZipEntry(entryName));

                InputStream in = null;

                try {

                    int read = 0;

                    in = file.getContents();

                    while ((read = in.read(buffer)) > 0) {

                        if (progress.isCanceled())
                            throw new OperationCanceledException(
                                "compressing of file '" + originalEntryName
                                    + "' was canceled");

                        zipStream.write(buffer, 0, read);

                        totalRead += read;

                        updateMonitor(progress, totalRead, totalSize);
                    }
                } finally {
                    IOUtils.closeQuietly(in);
                }
                zipStream.closeEntry();
            }

            zipStream.finish();
            cleanup = false;
        } catch (IOException e) {
            LOG.error("failed to create archive", e);
            throw new CoreException(new Status(IStatus.ERROR, Saros.SAROS,
                "failed to create archive", e));

        } finally {
            IOUtils.closeQuietly(zipStream);
            if (cleanup && archive != null && archive.exists()
                && !archive.delete())
                LOG.warn("could not delete archive file: " + archive);

            if (monitor != null)
                monitor.done();
        }

        stopWatch.stop();

        LOG.debug(String.format("created archive %s I/O: [%s]",
            archive.getAbsolutePath(),
            CoreUtils.throughput(archive.length(), stopWatch.getTime())));

    }

    private int lastWorked = 0;

    private void updateMonitor(final IProgressMonitor monitor,
        final long totalRead, final long totalSize) {

        if (totalSize == 0)
            return;

        int worked = (int) ((totalRead * 100L) / totalSize);
        int workedDelta = worked - lastWorked;

        if (workedDelta > 0) {
            monitor.worked(workedDelta);
            lastWorked = worked;
        }
    }
}
