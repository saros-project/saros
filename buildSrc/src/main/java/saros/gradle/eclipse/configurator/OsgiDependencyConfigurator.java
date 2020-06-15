package saros.gradle.eclipse.configurator;

import com.diffplug.gradle.eclipse.MavenCentralExtension;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Manifest;
import org.gradle.api.GradleException;
import org.gradle.api.Project;

public class OsgiDependencyConfigurator {

  private static final String REQUIRE_BUNDLE_KEY = "Require-Bundle";
  private static final String GOOMPH_MVN_CENTRAL_PLUGIN_ID = "com.diffplug.eclipse.mavencentral";

  private Project project;

  public OsgiDependencyConfigurator(Project project) {
    this.project = project;
    project.getPluginManager().apply(GOOMPH_MVN_CENTRAL_PLUGIN_ID);
  }

  /**
   * Add bundle dependencies as maven artifact dependencies.
   *
   * @param manifestFile The manifest file that contains a list of dependencies specified in the
   *     'Require-Bundle' entry.
   * @param eclipseVersion The eclipse version which implies the bundle version of the dependencies.
   */
  public void addDependencies(
      File manifestFile, List<String> excludeManifestDependencies, String eclipseVersion) {
    MavenCentralExtension goomphMavenCentralExtension =
        project.getExtensions().findByType(MavenCentralExtension.class);

    if (goomphMavenCentralExtension == null) {
      throw new GradleException(
          "Missing goomph mavenCentral plugin. Cannot find corresponding extension!");
    }

    String requireBundleValue = getRequireBundleValue(manifestFile);
    if (requireBundleValue == null) {
      throw new GradleException(
          "Unable to add dependencies, because the 'Require-Bundle' entry is missing in the manifest file: "
              + manifestFile.getAbsolutePath());
    }
    String[] bundles = parseRequireBundleValue(requireBundleValue, excludeManifestDependencies);

    try {
      goomphMavenCentralExtension.release(
          eclipseVersion,
          (MavenCentralExtension.ReleaseConfigurer c) -> {
            for (String bundleId : bundles) {
              c.implementation(bundleId);
            }
            c.useNativesForRunningPlatform();
          });
    } catch (IOException e) {
      throw new GradleException("Failed to configure the osgi bundle dependency resolution", e);
    }
  }

  /**
   * Returns the value of "Require-Bundle: <value>" of an osgi manifest.
   *
   * @param manifestFile Manifest file.
   * @return value of the Require-Bundle entry if available. Otherwise null.
   */
  private static String getRequireBundleValue(File manifestFile) {
    String bundleValue;

    try (FileInputStream manifestStream = new FileInputStream(manifestFile)) {
      Manifest manifest = new Manifest(manifestStream);
      bundleValue = manifest.getMainAttributes().getValue(REQUIRE_BUNDLE_KEY);
    } catch (IOException e) {
      throw new GradleException(
          "Unable to determine the bundle dependencies defined in manifest: "
              + manifestFile.getAbsolutePath(),
          e);
    }
    return bundleValue;
  }

  /**
   * Returns all bundles specified in the value string of a 'Require-Bundle' entry without the
   * bundles specified in {@code bundlesToExclude}.
   *
   * @param value The Require-Bundle entry value (e.g. {@code
   *     "org.eclipse.ui.forms,org.eclipse.jdt.core;resolution:=optional"}).
   * @param bundlesToExclude List of bundles to exclude (e.g. {@code ["saros.core"]}).
   * @return return array containing all required (not excluded) bundles specified in {@code value}.
   */
  private static String[] parseRequireBundleValue(String value, List<String> bundlesToExclude) {
    return Arrays.stream(value.split(","))
        .map(it -> it.split(";")[0])
        .filter(it -> !bundlesToExclude.contains(it))
        .toArray(String[]::new);
  }
}
