package saros.negotiation;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import saros.filesystem.FileSystem;
import saros.filesystem.IFile;
import saros.filesystem.IFolder;
import saros.filesystem.IProject;
import saros.filesystem.IResource;
import saros.filesystem.checksum.IChecksumCache;
import saros.monitoring.IProgressMonitor;
import saros.monitoring.NullProgressMonitor;
import saros.negotiation.FileList.MetaData;

/**
 * Offers two ways to create {@link FileList file lists}.
 *
 * <p>
 * <li>Either an inexpensive one that rescans the whole project to gather meta data:<br>
 *     {@link #createFileList(IProject, IChecksumCache, IProgressMonitor)}
 * <li>Or a cheap one which requires the caller to take care of the validity of input data:<br>
 *     {@link #createFileList(List)}
 */
public class FileListFactory {

  private static final Logger log = Logger.getLogger(FileListFactory.class);

  private FileListFactory() {
    // NOP
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

  /**
   * Creates a file list for the given project.
   *
   * <p>Uses the given checksum cache for the checksum calculation. Reports progress to the passed
   * progress monitor if present.
   *
   * @param project the project for which to create a file list
   * @param checksumCache the checksum cache to use during the checksum calculation
   * @param suggestedMonitor the progress monitor to report to or <code>null</code>
   * @return a file list for the given project
   * @throws IOException if the default charset for the project, the members contained in the
   *     project or one of its folders, or the charset of a contained file could not be obtained
   */
  public static FileList createFileList(
      final IProject project,
      final IChecksumCache checksumCache,
      final IProgressMonitor suggestedMonitor)
      throws IOException {

    FileList list = new FileList();

    list.addEncoding(project.getDefaultCharset());

    List<IFile> files = calculateMembers(list, project);

    IProgressMonitor monitor =
        suggestedMonitor != null ? suggestedMonitor : new NullProgressMonitor();

    calculateChecksums(list, files, checksumCache, monitor);

    return list;
  }

  /**
   * Calculates all files contained in the given project and adds them to the given file list.
   * Returns a list of all found files.
   *
   * @param list the file list
   * @param project the project for which to calculate the members
   * @return a list of all found files
   * @throws IOException if the members contained in the project or one of its folders or the
   *     charset of a contained file could not be obtained
   */
  private static List<IFile> calculateMembers(final FileList list, final IProject project)
      throws IOException {

    List<IResource> resources = Arrays.asList(project.members());

    if (resources.size() == 0) return Collections.emptyList();

    Deque<IResource> stack = new LinkedList<>(resources);

    List<IFile> files = new LinkedList<>();

    while (!stack.isEmpty()) {
      IResource resource = stack.pop();

      if (resource.isIgnored() || !resource.exists()) continue;

      String path = resource.getProjectRelativePath().toPortableString();

      if (list.contains(path)) continue;

      switch (resource.getType()) {
        case FILE:
          files.add((IFile) resource);
          MetaData data = new MetaData();
          list.addPath(path, data, false);
          list.addEncoding(((IFile) resource).getCharset());
          break;

        case FOLDER:
          stack.addAll(Arrays.asList(((IFolder) resource).members()));
          list.addPath(path, null, true);
          break;
      }
    }

    return files;
  }

  /**
   * Calculates the checksums of the given files and adds them to the given file list.
   *
   * @param list the file list
   * @param files the files for which to calculate the checksum
   * @param checksumCache the checksum cache to use during the checksum calculation
   * @param monitor the progress monitor to report to
   */
  private static void calculateChecksums(
      final FileList list,
      final List<IFile> files,
      final IChecksumCache checksumCache,
      final IProgressMonitor monitor) {

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

          if (isInvalid && checksum != null) log.warn("calculated checksum on dirty data: " + file);
        }

      } catch (IOException e) {
        log.error(e);
      }

      monitor.worked(1);
    }
  }
}
