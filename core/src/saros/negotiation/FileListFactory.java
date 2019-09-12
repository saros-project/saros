package saros.negotiation;

import java.io.IOException;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import saros.filesystem.FileSystem;
import saros.filesystem.IChecksumCache;
import saros.filesystem.IFile;
import saros.filesystem.IFolder;
import saros.filesystem.IProject;
import saros.filesystem.IResource;
import saros.monitoring.IProgressMonitor;
import saros.monitoring.NullProgressMonitor;
import saros.negotiation.FileList.MetaData;

/**
 * Offers two ways to create {@link FileList file lists}.
 *
 * <p>
 * <li>Either an inexpensive one that rescans the whole project to gather meta data:<br>
 *     {@link #createFileList(IProject, List, IChecksumCache, IProgressMonitor)}
 * <li>Or a cheap one which requires the caller to take care of the validity of input data:<br>
 *     {@link #createFileList(List)}
 */
public class FileListFactory {

  private static final Logger LOG = Logger.getLogger(FileListFactory.class);

  private IChecksumCache checksumCache;
  private IProgressMonitor monitor;

  private FileListFactory(IChecksumCache checksumCache, IProgressMonitor monitor) {
    this.checksumCache = checksumCache;
    this.monitor = monitor;

    if (this.monitor == null) this.monitor = new NullProgressMonitor();
  }

  public static FileList createFileList(
      IProject project,
      List<IResource> resources,
      IChecksumCache checksumCache,
      IProgressMonitor monitor)
      throws IOException {

    FileListFactory fact = new FileListFactory(checksumCache, monitor);
    return fact.build(project, resources);
  }

  /**
   * Creates a new file list from given paths. It does not compute checksums or location
   * information. Every path is treated as a file. It is <b>not</b> possible to add directories.
   *
   * <p><b>Note:</b> This method does not check the input. The caller is <b>responsible</b> for the
   * <b>correct</b> input !
   *
   * @param paths a list of paths that <b>refers</b> to <b>files</b> that should be added to this
   *     file list.
   */
  public static FileList createFileList(List<String> paths) {
    FileList list = new FileList();

    for (String path : paths) list.addPath(path);

    return list;
  }

  public static FileList createEmptyFileList() {
    return new FileList();
  }

  private FileList build(IProject project, List<IResource> resources) throws IOException {

    FileList list = new FileList();

    if (resources == null) {
      list.addEncoding(project.getDefaultCharset());
      resources = Arrays.asList(project.members());
    }

    addMembersToList(list, resources);

    return list;
  }

  private void addMembersToList(final FileList list, final List<IResource> resources)
      throws IOException {

    if (resources.size() == 0) return;

    Deque<IResource> stack = new LinkedList<IResource>();

    stack.addAll(resources);

    List<IFile> files = new LinkedList<IFile>();

    while (!stack.isEmpty()) {
      IResource resource = stack.pop();

      if (resource.isIgnored() || !resource.exists()) continue;

      String path = resource.getProjectRelativePath().toPortableString();

      if (list.contains(path)) continue;

      MetaData data = null;

      switch (resource.getType()) {
        case IResource.FILE:
          files.add((IFile) resource);
          data = new MetaData();
          list.addPath(path, data, false);
          list.addEncoding(((IFile) resource).getCharset());
          break;
        case IResource.FOLDER:
          stack.addAll(Arrays.asList(((IFolder) resource).members()));
          list.addPath(path, data, true);
          break;
      }
    }

    monitor.beginTask("Calculating checksums...", files.size());

    for (IFile file : files) {
      try {
        monitor.subTask(file.getProject().getName() + ": " + file.getProjectRelativePath());

        MetaData data = list.getMetaData(file.getProjectRelativePath().toPortableString());

        Long checksum = null;

        if (checksumCache != null) checksum = checksumCache.getChecksum(file);

        data.checksum = checksum == null ? FileSystem.checksum(file) : checksum;

        if (checksumCache != null) {
          boolean isInvalid = checksumCache.addChecksum(file, data.checksum);

          if (isInvalid && checksum != null)
            LOG.warn("calculated checksum on dirty data: " + file.getFullPath());
        }

      } catch (IOException e) {
        LOG.error(e);
      }

      monitor.worked(1);
    }
  }
}
