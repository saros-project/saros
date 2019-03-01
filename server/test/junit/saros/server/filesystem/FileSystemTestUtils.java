package saros.server.filesystem;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import saros.filesystem.IPath;
import saros.filesystem.IWorkspace;

/**
 * Provides utility methods for the server file system implementation tests. These methods are meant
 * to be imported statically to form a sort of domain-specific language for file system
 * implementation tests.
 */
class FileSystemTestUtils {

  /**
   * A shorthand form for creating an {@link IPath}. When imported statically, its use significantly
   * more compact than calling <code>ServerPathImpl.fromString(pathString)</code>.
   *
   * @param pathString the path string to create a path from
   * @return the resulting {@link IPath}
   */
  public static IPath path(String pathString) {
    return ServerPathImpl.fromString(pathString);
  }

  /**
   * Creates an absolute {@link IPath} with the segments specified by the passed relative path
   * string. This makes it easy to construct an absolute path in an operating-system-independent
   * way.
   *
   * @param pathString the path string to create the path from
   * @return the resulting {@link IPath}
   */
  public static IPath absolutePath(String pathString) {
    Path root = Paths.get("").toAbsolutePath().getRoot();
    Path relativePath = Paths.get(pathString);
    return ServerPathImpl.fromString(root.resolve(relativePath).toString());
  }

  /**
   * Creates a temporary folder to serve as a workspace root. Make sure to delete the folder after
   * use, e.g. by using {@link FileUtils#deleteQuietly}.
   *
   * @return temporary workspace root folder
   * @throws IOException
   */
  public static IPath createWorkspaceFolder() throws IOException {
    Path folderPath = Files.createTempDirectory("saros-test-workspace");
    return path(folderPath.toString());
  }

  /**
   * Creates an empty file in the specified workspace. Parent folders are created automatically if
   * needed.
   *
   * @param workspace workspace to create the file in
   * @param path workspace-relative path of the file to create
   * @throws IOException
   */
  public static void createFile(IWorkspace workspace, String path) throws IOException {

    createFile(workspace, path, null);
  }

  /**
   * Creates an UTF-8 text file in the specified workspace. Parent folders are created automatically
   * if needed.
   *
   * @param workspace workspace to create the file in
   * @param path workspace-relative path of the file to create
   * @param content the created file's text content, or <code>null</code> to create an empty file
   * @throws IOException if the file creation fails
   */
  public static void createFile(IWorkspace workspace, String path, String content)
      throws IOException {

    IPath absolutePath = workspace.getLocation().append(path);
    Path absoluteNioPath = nioPath(absolutePath);

    Files.createDirectories(absoluteNioPath.getParent());
    Files.createFile(absoluteNioPath);

    if (content != null) {
      Files.write(absoluteNioPath, content.getBytes("UTF-8"));
    }
  }

  /**
   * Creates a folder within the specified workspace. Parent folder are created automatically if
   * needed.
   *
   * @param workspace workspace to create the folder in
   * @param path workspace-relative path of the folder to create
   * @throws IOException if the folder creation fails
   */
  public static void createFolder(IWorkspace workspace, String path) throws IOException {

    IPath absolutePath = workspace.getLocation().append(path);
    Files.createDirectories(nioPath(absolutePath));
  }

  /**
   * Asserts that a workspace resource exists at the specified path.
   *
   * @param workspace the workspace to do the check in
   * @param path the path to check
   */
  public static void assertResourceExists(IWorkspace workspace, String path) {
    assertTrue(resourceLocation(workspace, path).toFile().exists());
  }

  /**
   * Asserts that no workspace resource exists at the specified path.
   *
   * @param workspace the workspace to do the check in
   * @param path the path to check
   */
  public static void assertResourceNotExists(IWorkspace workspace, String path) {
    assertFalse(resourceLocation(workspace, path).toFile().exists());
  }

  /**
   * Asserts that a workspace resource is exists as a folder in the local filesystem.
   *
   * @param workspace the workspace to do the check in
   * @param path the path to check
   */
  public static void assertIsFolder(IWorkspace workspace, String path) {
    assertTrue(resourceLocation(workspace, path).toFile().isDirectory());
  }

  /**
   * Asserts that a workspace file has the specified UTF-8 text content.
   *
   * @param workspace the workspace to do the check in
   * @param path path of the file whose content to check
   * @param expectedContent the text content that the file should have
   * @throws IOException if reading the file fails
   */
  public static void assertFileHasContent(IWorkspace workspace, String path, String expectedContent)
      throws IOException {

    IPath location = resourceLocation(workspace, path);
    byte[] actualContent = Files.readAllBytes(nioPath(location));
    assertArrayEquals(expectedContent.getBytes("UTF-8"), actualContent);
  }

  private static Path nioPath(IPath path) {
    return ((ServerPathImpl) path).getDelegate();
  }

  private static IPath resourceLocation(IWorkspace workspace, String path) {
    return workspace.getLocation().append(path);
  }
}
