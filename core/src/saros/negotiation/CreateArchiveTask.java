package saros.negotiation;

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
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import saros.exceptions.OperationCanceledException;
import saros.filesystem.IFile;
import saros.filesystem.IWorkspaceRunnable;
import saros.monitoring.IProgressMonitor;
import saros.monitoring.NullProgressMonitor;
import saros.util.CoreUtils;

// TODO java doc
public class CreateArchiveTask implements IWorkspaceRunnable {

  private static final int BUFFER_SIZE = 32 * 1024;

  private static final Logger log = Logger.getLogger(CreateArchiveTask.class);

  private final File archive;
  private final List<Pair<IFile, String>> filesToCompress;
  private final IProgressMonitor monitor;

  public CreateArchiveTask(
      final File archive,
      final List<Pair<IFile, String>> filesToCompress,
      final IProgressMonitor monitor) {

    this.archive = archive;
    this.filesToCompress = filesToCompress;
    this.monitor = monitor;
  }

  @Override
  public void run(IProgressMonitor monitor) throws IOException, OperationCanceledException {
    if (this.monitor != null) monitor = this.monitor;

    if (monitor == null) monitor = new NullProgressMonitor();

    long totalSize = getTotalFileSize(filesToCompress);

    StopWatch stopWatch = new StopWatch();
    stopWatch.start();

    final Iterator<Pair<IFile, String>> fileIt = filesToCompress.iterator();

    long totalRead = 0L;

    boolean cleanup = true;

    byte[] buffer = new byte[BUFFER_SIZE];

    ZipOutputStream zipStream = null;

    monitor.beginTask("Compressing files...", 100 /* percent */);

    try {
      zipStream =
          new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(archive), BUFFER_SIZE));

      while (fileIt.hasNext()) {
        Pair<IFile, String> fileToCompress = fileIt.next();

        IFile file = fileToCompress.getLeft();
        String qualifiedPath = fileToCompress.getRight();

        if (log.isTraceEnabled()) log.trace("compressing file: " + qualifiedPath);

        monitor.subTask("compressing file: " + qualifiedPath);

        zipStream.putNextEntry(new ZipEntry(qualifiedPath));

        InputStream in = null;

        try {

          int read = 0;

          in = file.getContents();

          while ((read = in.read(buffer)) > 0) {

            if (monitor.isCanceled())
              throw new OperationCanceledException(
                  "compressing of file '" + qualifiedPath + "' was canceled");

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
        log.warn("could not delete archive file: " + archive);

      monitor.done();
    }

    stopWatch.stop();

    log.debug(
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

  private long getTotalFileSize(List<Pair<IFile, String>> filesToCompress) {

    long size = 0L;

    for (Pair<IFile, String> fileToCompare : filesToCompress) {
      IFile file = fileToCompare.getLeft();

      try {
        size += file.getSize();
      } catch (IOException e) {
        log.warn("unable to retrieve file size for file: " + file, e);
      }
    }

    return size;
  }
}
