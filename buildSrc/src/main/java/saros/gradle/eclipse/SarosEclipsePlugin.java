package saros.gradle.eclipse;

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import saros.gradle.eclipse.configurator.EclipseConfigurator;
import saros.gradle.eclipse.configurator.JarConfigurator;
import saros.gradle.eclipse.configurator.OsgiDependencyConfigurator;

/**
 * Plugin that provides the default saros specific default configurations of the eclipse plugin and
 * provides methods in order to perform common configuration tasks.
 */
public class SarosEclipsePlugin implements Plugin<Project> {
  private static final String EXTENSION_NAME = "sarosEclipse";

  /**
   * Method which is called when the plugin is integrated in a gradle build (e.g. with {@code apply
   * plugin: saros.gradle.eclipse.plugin})
   */
  @Override
  public void apply(Project project) {
    // We just configure other plugins and their corresponding tasks. Therefore, we
    // don't create custom tasks.
    SarosEclipseExtension e =
        project.getExtensions().create(EXTENSION_NAME, SarosEclipseExtension.class);
    project.afterEvaluate(p -> configureEclipseAfterEvaluate(p, e));
  }

  /**
   * Method that applies the configurations which are configured in the build.gradle file. This
   * method has to be executed after the evaluation of the build files is completed. Otherwise an
   * intermediate state of the extensions are used.
   */
  private void configureEclipseAfterEvaluate(Project p, SarosEclipseExtension e) {
    EclipseConfigurator eclipseConfigurator = new EclipseConfigurator(p);
    eclipseConfigurator.usePatchedPicocontainer();
    if (e.isAddPdeNature()) {
      eclipseConfigurator.addPdeNature();
    }

    if (e.isCreateBundleJar()) {
      methodRequiresManifest("createBundleJar", e);
      new JarConfigurator(p).createBundleJar(e.getManifest());
    }

    if (e.isAddDependencies()) {
      methodRequiresManifest("addDependencies", e);
      new OsgiDependencyConfigurator(p)
          .addDependencies(
              e.getManifest(), e.getExcludeManifestDependencies(), e.getEclipseVersion());
    }
  }

  private void methodRequiresManifest(String methodName, SarosEclipseExtension e) {
    if (e.getManifest() == null)
      throw new GradleException(
          "Unable to apply method "
              + methodName
              + " as long as no manifest is provided. Please set the manifest before calling "
              + methodName
              + ".");
  }
}
