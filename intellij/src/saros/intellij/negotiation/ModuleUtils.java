package saros.intellij.negotiation;

import com.intellij.openapi.roots.SourceFolder;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Provides utility methods to interact with modules. */
class ModuleUtils {
  private static final Logger log = Logger.getLogger(ModuleUtils.class);

  /**
   * Returns the relative path from the given base path to the relative source folder.
   *
   * @param basePath the base path to construct the relative path from
   * @param sourceFolder the source folder to construct the relative path for
   * @return the relative path from the given base path to the relative source folder or <code>null
   *     </code> if the relative path could not be constructed
   * @throws AssertionError when the path of the source folder does not start with the given base
   *     path
   */
  @Nullable
  static Path getRelativeRootPath(@NotNull Path basePath, @NotNull SourceFolder sourceFolder) {
    String sourcePath = sourceFolder.getUrl();
    try {
      // Workaround to convert the URI to the canonical path needed for Windows paths
      File childFile = new File(new URI(sourcePath).getPath());
      Path childPath = Paths.get(childFile.getCanonicalPath());

      assert childPath.startsWith(basePath)
          : "Encountered path that is not located below the given base directory "
              + basePath
              + " - "
              + childPath;

      return basePath.relativize(childPath);

    } catch (URISyntaxException e) {
      log.warn("Could not parse source folder url", e);

    } catch (IOException e) {
      log.warn("Could not make source folder path canonical", e);

    } catch (IllegalArgumentException e) {
      log.warn("Could not construct relative path for the given source folder", e);
    }

    return null;
  }
}
