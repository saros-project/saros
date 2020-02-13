package saros.gradle.eclipse.configurator;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.gradle.api.GradleException;

/**
 * Class that provides methods to change the eclipse plugin version defined in an osgi manifest
 * files.
 */
public class OsgiBundleVersionConfigurator {
  private final File manifest;

  public OsgiBundleVersionConfigurator(File manifest) {
    this.manifest = manifest;
  }

  /**
   * Adds a qualifier to the existing version entry. (e.g. version '15.0.0' with qualifier
   * '.NIGHTLY' becomes '15.0.0.NIGHTLY')
   *
   * @param qualifier Qualifier that is appended to the version
   */
  public void addQualifier(String qualifier) {
    if (qualifier == null || qualifier.isEmpty()) return;
    replaceLines("^(Bundle-Version:\\s*[0-9\\.]+)\\s*$", "$1" + qualifier);
  }

  private void replaceLines(String regex, String replacement) {
    try {
      Path path = manifest.toPath();
      Charset charset = StandardCharsets.UTF_8;
      List<String> oldContentLines = Files.readAllLines(path, charset);
      String newContent =
          oldContentLines
              .stream()
              .map(line -> line.replaceAll(regex, replacement))
              .collect(Collectors.joining("\n"));
      Files.write(path, newContent.getBytes(charset));
    } catch (IOException e) {
      throw new GradleException("Failed to replace manfest content", e);
    }
  }
}
