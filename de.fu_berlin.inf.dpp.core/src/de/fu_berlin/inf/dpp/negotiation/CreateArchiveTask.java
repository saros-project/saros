package de.fu_berlin.inf.dpp.negotiation;

import de.fu_berlin.inf.dpp.exceptions.OperationCanceledException;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IWorkspaceRunnable;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import de.fu_berlin.inf.dpp.monitoring.NullProgressMonitor;
import de.fu_berlin.inf.dpp.util.CoreUtils;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.Logger;

// TODO java doc
public class CreateArchiveTask implements IWorkspaceRunnable {

  private static final int BUFFER_SIZE = 32 * 1024;

  private static final Logger LOG = Logger.getLogger(CreateArchiveTask.class);

  private final File archive;
  private final List<IFile> files;
  private final List<String> alias;
  private final IProgressMonitor monitor;

  public CreateArchiveTask(
      final File archive,
      final List<IFile> files,
      final List<String> alias,
      final IProgressMonitor monitor) {
    this.archive = archive;
    this.files = files;
    this.alias = alias;
    this.monitor = monitor;
  }

  @Override
  public void run(IProgressMonitor monitor) throws IOException, OperationCanceledException {
    if (this.monitor != null) monitor = this.monitor;

    if (monitor == null) monitor = new NullProgressMonitor();

    assert files.size() == alias.size();

    long totalSize = getTotalFileSize(files);

    StopWatch stopWatch = new StopWatch();
    stopWatch.start();

    final Iterator<IFile> fileIt = files.iterator();
    final Iterator<String> aliasIt = alias == null ? null : alias.iterator();

    long totalRead = 0L;

    boolean cleanup = true;

    byte[] buffer = new byte[BUFFER_SIZE];

    ZipOutputStream zipStream = null;

    monitor.beginTask("Compressing files...", 100 /* percent */);

    try {
      zipStream =
          new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(archive), BUFFER_SIZE));

      while (fileIt.hasNext()) {

        IFile file = fileIt.next();

        String entryName = null;

        final String originalEntryName = file.getFullPath().toString();

        if (aliasIt != null && aliasIt.hasNext()) entryName = aliasIt.next();

        if (entryName == null) entryName = originalEntryName;

        if (LOG.isTraceEnabled()) LOG.trace("compressing file: " + originalEntryName);

        monitor.subTask("compressing file: " + originalEntryName);

        zipStream.putNextEntry(new ZipEntry(entryName));

        InputStream in = null;

        try {

          int read = 0;

          in = file.getContents();

          while ((read = in.read(buffer)) > 0) {

            if (monitor.isCanceled())
              throw new OperationCanceledException(
                  "compressing of file '" + originalEntryName + "' was canceled");

            zipStream.write(buffer, 0, read);

            totalRead += read;

            updateMonitor(monitor, totalRead, totalSize);
          }
        } finally {
          IOUtils.closeQuietly(in);
        }
        zipStream.closeEntry();
      }

      zipStream.finish();
      cleanup = false;
    } finally {
      IOUtils.closeQuietly(zipStream);
      if (cleanup && archive != null && archive.exists() && !archive.delete())
        LOG.warn("could not delete archive file: " + archive);

      monitor.done();
    }

    stopWatch.stop();

    LOG.debug(
        String.format(
            "created archive %s I/O: [%s]",
            archive.getAbsolutePath(),
            CoreUtils.throughput(archive.length(), stopWatch.getTime())));
  }

  private int lastWorked = 0;

  private void updateMonitor(
      final IProgressMonitor monitor, final long totalRead, final long totalSize) {

    if (totalSize == 0) return;

    int worked = (int) ((totalRead * 100L) / totalSize);
    int workedDelta = worked - lastWorked;

    if (workedDelta > 0) {
      monitor.worked(workedDelta);
      lastWorked = worked;
    }
  }

  private final long getTotalFileSize(Collection<IFile> files) {

    long size = 0L;

    for (IFile file : files) {
      try {
        size += file.getSize();
      } catch (IOException e) {
        LOG.warn("unable to retrieve file size for file: " + file.getFullPath(), e);
      }
    }

    return size;
  }
}
