package saros.server.filesystem;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.io.FileUtils;
import saros.activities.SPath;
import saros.filesystem.IPath;
import saros.filesystem.IReferencePoint;
import saros.filesystem.ReferencePointImpl;

/**
 * The ServerReferencePointManager maps an {@link IReferencePoint} reference point to {@link File}
 * folder
 */
public class ServerReferencePointManager {
  private final ConcurrentHashMap<IReferencePoint, File> referencePointToFileMapper;

  public ServerReferencePointManager() {
    referencePointToFileMapper = new ConcurrentHashMap<>();
  }

  /**
   * Create an {@link IReferencePoint} reference point given by {@link File} folder
   *
   * @param folder for which the reference point should be created
   * @return reference point of given directory or null if the given folder is not a directory
   */
  public static IReferencePoint create(File folder) {
    if (!folder.isDirectory()) return null;

    IPath path = ServerPathImpl.fromString(folder.getPath());

    return new ReferencePointImpl(path);
  }

  /**
   * Insert a pair of {@link IReferencePoint} reference point and {@link File} directory
   *
   * @param referencePoint which should be inserted to the ServerReferencePointManager
   * @param directory which should be inserted to the ServerReferencePointManager
   * @exception IllegalArgumentException if {@link IReferencePoint} reference point is null
   * @exception IllegalArgumentException if {@link File} directory is null
   */
  public synchronized void putIfAbsent(IReferencePoint referencePoint, File directory) {
    checkArgument(referencePoint, "ReferencePoint is null!");
    checkArgument(directory, "Directory is null!");

    referencePointToFileMapper.putIfAbsent(referencePoint, directory);
  }

  /**
   * The ServerReferencePointManager determines the {@link IReferencePoint} reference point of the
   * given {@link File} directory and put them to the ServerReferencePointManager
   *
   * @param directory which should be inserted to the ServerReferencePointManager
   * @exception IllegalArgumentException if {@link File} directory is null
   */
  public synchronized void putIfAbsent(File directory) {
    checkArgument(directory, "Directory is null!");

    IReferencePoint referencePoint = create(directory);

    referencePointToFileMapper.putIfAbsent(referencePoint, directory);
  }

  /**
   * The ServerReferencePointManager determines the {@link File} directory and {@link
   * IReferencePoint} reference point of the given {@link Path} path and put them to the
   * ServerReferencePointManager
   *
   * @param path to the directory which should be inserted to the ServerReferencePointManager
   * @exception IllegalArgumentException if {@link Path} path is null
   * @exception UnsupportedOperationException if {@link Path} path is not associated with the
   *     default provider
   */
  public synchronized void putIfAbsent(Path path) {
    checkArgument(path, "Path is null!");

    File directory = path.toFile();

    putIfAbsent(directory);
  }

  /**
   * Returns the {@link File} directory given by the {@link IReferencePoint}
   *
   * @param referencePoint the key for which the directory should be returned
   * @return the directory given by referencePoint
   * @exception IllegalArgumentException if {@link IReferencePoint} reference point is null
   */
  public synchronized File getIoFolder(IReferencePoint referencePoint) {
    checkArgument(referencePoint, "ReferencePoint is null!");

    return referencePointToFileMapper.get(referencePoint);
  }

  /**
   * Creates a {@link File} file given by a {@link SPath} SPath and fills it with the given content
   * if not null
   *
   * @param sPath path to the file which should be created
   * @param content content of the new file
   * @exception IOException if an I/O error occurs
   */
  public synchronized void createFile(SPath sPath, byte[] content) throws IOException {
    Path nioPath = ((ServerPathImpl) sPath.getFullPath()).getDelegate();

    Files.createDirectories(nioPath.getParent());
    Files.createFile(nioPath);
    Path tempFilePath = Files.createTempFile(sPath.getFullPath().lastSegment(), null);

    if (content != null) {
      Files.copy(
          new ByteArrayInputStream(content), tempFilePath, StandardCopyOption.REPLACE_EXISTING);
    }

    moveTo(tempFilePath, ((ServerPathImpl) sPath.getFullPath()).getDelegate());
  }

  /**
   * Moves a {@link File} resource from the old {@link Path} path to the new {@link Path} path.
   *
   * @param oldPath old path of the file
   * @param newPath new path of the file
   * @param content content of file
   * @throws IOException if an I/O error occurs
   */
  public synchronized void moveResource(SPath oldPath, SPath newPath, byte[] content)
      throws IOException {
    File oldFile = getResource(oldPath);
    File newFile = getResource(newPath);

    moveTo(oldFile.toPath(), newFile.toPath());

    if (content != null) {
      Files.copy(
          new ByteArrayInputStream(content), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
  }

  /**
   * Deletes the {@link File} which is located in {@link SPath} path
   *
   * @param sPath path to the file
   * @exception IOException if an I/O error occurs
   */
  public synchronized void deleteFile(SPath sPath) throws IOException {
    Files.delete(((ServerPathImpl) sPath.getFullPath()).getDelegate());
  }

  /**
   * Creates a {@link File} folder given by {@link SPath} path
   *
   * @param sPath path to the folder
   * @exception IOException if an I/O error occurs
   */
  public synchronized void createFolder(SPath sPath) throws IOException {
    try {
      Files.createDirectory(((ServerPathImpl) sPath.getFullPath()).getDelegate());
    } catch (FileAlreadyExistsException e) {
      /*
       * That the resource already exists is only a problem for us if it's
       * not a directory.
       */
      if (!Files.isDirectory(Paths.get(e.getFile()))) {
        throw e;
      }
    }
  }

  /**
   * Deletes the {@link File} folder given by {@link SPath} path
   *
   * @param sPath path to the folder
   * @exception IOException if an I/O error occurs
   */
  public synchronized void deleteFolder(SPath sPath) throws IOException {
    FileUtils.deleteDirectory(sPath.getFullPath().toFile());
  }

  /**
   * Returns the {@link File} resource resource which is represented by {@link SPath} SPath or null
   * if the resource does not exist
   *
   * @param path path which represents the resource
   * @return return the resource or null if the resource does not exists
   */
  public synchronized File getResource(SPath path) {
    return getResource(path.getReferencePoint(), path.getProjectRelativePath());
  }

  /**
   * Returns the {@link File} resource in combination of the {@link IReferencePoint} reference point
   * and the {@link IPath} relative path from the reference point to the resource.
   *
   * @param referencePoint The reference point, on which the resource belongs to
   * @param referencePointRelativePath the relative path from the reference point to the resource
   * @return the resource of the reference point from referencePointRelativePath
   * @exception IllegalArgumentException if {@link IReferencePoint} reference point is null
   * @exception IllegalArgumentException if {@link IPath} reference point is null
   */
  public synchronized File getResource(
      IReferencePoint referencePoint, IPath referencePointRelativePath) {
    checkArgument(referencePoint, "ReferencePoint is null!");
    checkArgument(referencePoint, "Relative path is null!");

    File directory = getIoFolder(referencePoint);

    if (directory == null)
      throw new NullPointerException(
          "For reference point " + referencePoint + " does'nt exist a directoy.");

    return findResource(directory, referencePointRelativePath);
  }

  /**
   * Finds a {@link File} resource given by {@link File} directory as root and the {@link IPath}
   * relative path or null if the resource does not exist
   *
   * @param directory in which the resource is located
   * @param path relative path to the resource
   * @return the resource or null if the resource does not exist
   */
  private File findResource(final File directory, IPath path) {

    File file = new File(directory.getPath().concat(path.toString()));

    if (!file.exists()) return null;

    return file;
  }

  /**
   * Moves a {@link File} resource from the old {@link Path} path to the new {@link Path} path.
   *
   * @param oldPath old path of the file
   * @param newPath new path of the file
   * @throws IOException if an I/O error occurs
   */
  private void moveTo(Path oldPath, Path newPath) throws IOException {
    try {
      Files.move(
          oldPath, newPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    } catch (AtomicMoveNotSupportedException e) {
      Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
    }
  }

  /**
   * Check, if the given argument is null
   *
   * @param argument which is checked for null
   * @param <T> Type of argument parameter (like {@link IReferencePoint reference point})
   * @param message message for the IllegalArgumentException
   * @exception IllegalArgumentException if given argument is null
   */
  private <T> void checkArgument(T argument, String message) {
    if (argument == null) throw new IllegalArgumentException(message);
  }
}
