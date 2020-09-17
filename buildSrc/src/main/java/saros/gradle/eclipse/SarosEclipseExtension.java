package saros.gradle.eclipse;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Gradle extension that contains the configurable properties of the custom gradle plugin. The
 * provided setter and getters are used implicitly by gradle.
 *
 * <p>Furthermore, the class provides methods in order to perfom common action for osgi bundle
 * consumption and creation
 *
 * <p>Example gradle configuration that uses dependencies defined by a manfiest and excludes some
 * dependencies from resultion:
 *
 * <pre>
 * sarosEclipse {
 *   manifest = "META-INF/MANIFEST.MF" as File // Uses the setManifest(File manifest) method
 *   excludeManifestDependencies = ["org.eclipse.ui", "saros.core"]
 *   addDependencies()
 * }
 * </pre>
 */
public class SarosEclipseExtension {
  private File manifest = null;
  private String eclipseVersion = null;
  private String pluginVersionQualifier = null;
  private List<String> excludeManifestDependencies = new ArrayList<>();
  private boolean addDependencies = false;
  private boolean addPdeNature = false;
  private boolean createBundleJar = false;

  public File getManifest() {
    return manifest;
  }

  /**
   * Set the manifest file which is required to use createBundleJar (because a manifest is required
   * in order to create a valid osgi bundle) and addDependencies (because the dependencies have to
   * be specified in the manifest). Therefore, set the manifest before using addDependencies or
   * createBundleJar!
   *
   * @param manifest The java/osgi manifest file of the project
   */
  public void setManifest(File manifest) {
    this.manifest = manifest;
  }

  public String getEclipseVersion() {
    return eclipseVersion;
  }

  public void setPluginVersionQualifier(String qualifier) {
    this.pluginVersionQualifier = qualifier;
  }

  public String getPluginVersionQualifier() {
    return this.pluginVersionQualifier;
  }

  /**
   * Set the eclipse version which is used in order resolve the bundle dependencies during
   * addDependencies. Therefore, set the version before using addDependencies if you want to use
   * another version than the default version!
   */
  public void setEclipseVersion(String eclipseVersion) {
    this.eclipseVersion = eclipseVersion;
  }

  public List<String> getExcludeManifestDependencies() {
    return excludeManifestDependencies;
  }

  /**
   * Set an exclude list of bundles which are excluded from the manifest specified by {@code
   * setManifest(File manifest)} and resolved by {@code addDependencies()}. Therefore, set the list
   * before {@code addDependencies()} if you want to exclude bundles!
   *
   * @param manifestDepExcludes The list of bundle ids which should be excluded from the manifest.
   */
  public void setExcludeManifestDependencies(List<String> manifestDepExcludes) {
    this.excludeManifestDependencies = manifestDepExcludes;
  }

  public boolean isAddDependencies() {
    return addDependencies;
  }

  public void setAddDependencies(boolean addDependencies) {
    this.addDependencies = addDependencies;
  }

  public boolean isAddPdeNature() {
    return addPdeNature;
  }

  public void setAddPdeNature(boolean addPdeNature) {
    this.addPdeNature = addPdeNature;
  }

  public boolean isCreateBundleJar() {
    return createBundleJar;
  }

  public void setCreateBundleJar(boolean createBundleJar) {
    this.createBundleJar = createBundleJar;
  }
}
