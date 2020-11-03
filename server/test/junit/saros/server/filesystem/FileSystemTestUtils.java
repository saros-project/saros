package saros.server.filesystem;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;

/**
 * Provides utility methods for the server file system implementation tests. These methods are meant
 * to be imported statically to form a sort of domain-specific language for file system
 * implementation tests.
 */
class FileSystemTestUtils {

  /**
   * A shorthand form for creating an {@link Path}. When imported statically, it is more compact
   * than calling <code>Paths.get(pathString)</code>.
   *
   * @param pathString the path string to create a path from
   * @return the resulting {@link Path}
   */
  public static Path path(String pathString) {
    return Paths.get(pathString);
  }

  /**
   * Creates a temporary folder to serve as a workspace root. Make sure to delete the folder after
   * use, e.g. by using {@link FileUtils#deleteQuietly}.
   *
   * @return temporary workspace root folder
   * @throws IOException
   */
  public static Path createWorkspaceFolder() throws IOException {
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
  public static void createFile(ServerWorkspaceImpl workspace, String path) throws IOException {

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
  public static void createFile(ServerWorkspaceImpl workspace, String path, String content)
      throws IOException {

    Path absolutePath = workspace.getLocation().resolve(path);

    Files.createDirectories(absolutePath.getParent());
    Files.createFile(absolutePath);

    if (content != null) {
      Files.write(absolutePath, content.getBytes("UTF-8"));
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
  public static void createFolder(ServerWorkspaceImpl workspace, String path) throws IOException {
    Path absolutePath = workspace.getLocation().resolve(path);
    Files.createDirectories(absolutePath);
  }

  /**
   * Asserts that a workspace resource exists at the specified path.
   *
   * @param workspace the workspace to do the check in
   * @param path the path to check
   */
  public static void assertResourceExists(ServerWorkspaceImpl workspace, String path) {
    assertTrue(resourceLocation(workspace, path).toFile().exists());
  }

  /**
   * Asserts that no workspace resource exists at the specified path.
   *
   * @param workspace the workspace to do the check in
   * @param path the path to check
   */
  public static void assertResourceNotExists(ServerWorkspaceImpl workspace, String path) {
    assertFalse(resourceLocation(workspace, path).toFile().exists());
  }

  /**
   * Asserts that a workspace resource is exists as a folder in the local filesystem.
   *
   * @param workspace the workspace to do the check in
   * @param path the path to check
   */
  public static void assertIsFolder(ServerWorkspaceImpl workspace, String path) {
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
  public static void assertFileHasContent(
      ServerWorkspaceImpl workspace, String path, String expectedContent) throws IOException {

    Path location = resourceLocation(workspace, path);
    byte[] actualContent = Files.readAllBytes(location);
    assertArrayEquals(expectedContent.getBytes("UTF-8"), actualContent);
  }

  private static Path resourceLocation(ServerWorkspaceImpl workspace, String path) {
    return workspace.getLocation().resolve(path);
  }
}
