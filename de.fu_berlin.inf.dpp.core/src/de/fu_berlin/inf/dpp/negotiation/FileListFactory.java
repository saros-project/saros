package de.fu_berlin.inf.dpp.negotiation;

import de.fu_berlin.inf.dpp.filesystem.FileSystem;
import de.fu_berlin.inf.dpp.filesystem.IChecksumCache;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IFolder_V2;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import de.fu_berlin.inf.dpp.monitoring.NullProgressMonitor;
import de.fu_berlin.inf.dpp.negotiation.FileList.MetaData;
import de.fu_berlin.inf.dpp.session.IReferencePointManager;
import java.io.IOException;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * Offers two ways to create {@link FileList file lists}.
 *
 * <p>
 * <li>Either an inexpensive one that rescans the whole reference point to gather meta data:<br>
 *     {@link #createFileList(IReferencePointManager, IReferencePoint, List, IChecksumCache,
 *     IProgressMonitor)}
 * <li>Or a cheap one which requires the caller to take care of the validity of input data:<br>
 *     {@link #createFileList(List)}
 */
public class FileListFactory {

  private static final Logger LOG = Logger.getLogger(FileListFactory.class);

  private IChecksumCache checksumCache;
  private IProgressMonitor monitor;
  private IReferencePointManager referencePointManager;

  private FileListFactory(
      IChecksumCache checksumCache,
      IProgressMonitor monitor,
      IReferencePointManager referencePointManager) {
    this.checksumCache = checksumCache;
    this.monitor = monitor;
    this.referencePointManager = referencePointManager;

    if (this.monitor == null) this.monitor = new NullProgressMonitor();
  }

  public static FileList createFileList(
      IReferencePointManager referencePointManager,
      IReferencePoint referencePoint,
      List<IResource> resources,
      IChecksumCache checksumCache,
      IProgressMonitor monitor)
      throws IOException {

    FileListFactory fact = new FileListFactory(checksumCache, monitor, referencePointManager);
    return fact.build(referencePoint, resources);
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

  private FileList build(IReferencePoint referencePoint, List<IResource> resources)
      throws IOException {

    FileList list = new FileList();

    if (resources == null) {
      list.addEncoding(referencePointManager.get(referencePoint).getDefaultCharset());
      resources = Arrays.asList(referencePointManager.get(referencePoint).members());
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

      if (resource.isDerived() || !resource.exists()) continue;

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
          stack.addAll(Arrays.asList(((IFolder_V2) resource).members()));
          list.addPath(path, data, true);
          break;
      }
    }

    monitor.beginTask("Calculating checksums...", files.size());

    for (IFile file : files) {
      try {
        monitor.subTask(file.getReferenceFolder().getName() + ": " + file.getProjectRelativePath());

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
