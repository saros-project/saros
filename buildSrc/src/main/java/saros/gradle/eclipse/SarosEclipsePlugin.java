package saros.gradle.eclipse;

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import saros.gradle.eclipse.configurator.EclipseConfigurator;
import saros.gradle.eclipse.configurator.JarConfigurator;
import saros.gradle.eclipse.configurator.OsgiBundleVersionConfigurator;
import saros.gradle.eclipse.configurator.OsgiDependencyConfigurator;

/**
 * Plugin that provides the default saros specific default configurations of the eclipse plugin and
 * provides methods in order to perform common configuration tasks.
 */
public class SarosEclipsePlugin implements Plugin<Project> {
  private static final String EXTENSION_NAME = "sarosEclipse";
  private static final String PLUGIN_VERSION_CHANGE_TASK_NAME = "changeEclipsePluginVersion";

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
    project.afterEvaluate(
        p -> {
          configureEclipseAfterEvaluate(p, e);
          createPluginVersionChangeTask(p, e);
        });
  }

  private void createPluginVersionChangeTask(Project p, SarosEclipseExtension e) {
    String qualifier = e.getPluginVersionQualifier();
    if (qualifier == null || qualifier.trim().isEmpty()) return;

    p.getTasks()
        .register(
            PLUGIN_VERSION_CHANGE_TASK_NAME,
            (task) -> {
              task.getInputs().property("versionQualifier", qualifier);

              task.doLast(
                  (t) -> {
                    methodRequiresManifest("set version qualifier", e);
                    new OsgiBundleVersionConfigurator(e.getManifest()).addQualifier(qualifier);
                  });
            });

    Task jarTask = p.getTasks().findByPath("jar");
    if (jarTask == null)
      throw new GradleException("Unable to find jar task in project " + p.getName());
    jarTask.dependsOn(PLUGIN_VERSION_CHANGE_TASK_NAME);
  }

  /**
   * Method that applies the configurations which are configured in the build.gradle file. This
   * method has to be executed after the evaluation of the build files is completed. Otherwise an
   * intermediate state of the extensions are used.
   */
  private void configureEclipseAfterEvaluate(Project p, SarosEclipseExtension e) {
    EclipseConfigurator eclipseConfigurator = new EclipseConfigurator(p);
    if (e.isAddPdeNature()) {
      eclipseConfigurator.addPdeNature();
    }

    if (e.isCreateBundleJar()) {
      methodRequiresManifest("create bundle jar", e);
      new JarConfigurator(p).createBundleJar(e.getManifest());
    }

    if (e.isAddDependencies()) {
      methodRequiresManifest("add dependencies", e);
      String eclipseVersion = e.getEclipseVersion();
      if (eclipseVersion == null)
        throw new GradleException(
            "Unable to add osgi dependencies without an eclipse version specification.");

      new OsgiDependencyConfigurator(p)
          .addDependencies(e.getManifest(), e.getExcludeManifestDependencies(), eclipseVersion);
    }
  }

  private void methodRequiresManifest(String methodName, SarosEclipseExtension e) {
    if (e.getManifest() == null)
      throw new GradleException(
          "Unable to "
              + methodName
              + " as long as no manifest is provided. Please set the manifest before calling "
              + methodName
              + ".");
  }
}
