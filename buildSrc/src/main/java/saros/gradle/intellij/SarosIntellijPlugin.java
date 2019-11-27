package saros.gradle.intellij;

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jetbrains.intellij.IntelliJPlugin;
import org.jetbrains.intellij.IntelliJPluginExtension;

/**
 * Plugin that configures the gradle intellij plugin. The used plugin version is specified by the
 * build.gradle file in the buildSrc dir.
 *
 * <p>The main purpose of this plugin is to set default configs and to add additional tasks to the
 * runIde task (provided by the intellij plugin) in order to allow us to run multiple intellij
 * instances simultaneously with runIde. Without our custom tasks the second runIde call would
 * fails, because the config directory (within the build dir of the corresponding project) is
 * already in use. Therefore we enhance the config directory selection. See {@link
 * IntellijConfigurator} for more information.
 */
public class SarosIntellijPlugin implements Plugin<Project> {
  private static final String EXTENSION_NAME = "sarosIntellij";

  /**
   * Method which is called when the plugin is integrated in a gradle build (e.g. with {@code apply
   * plugin: saros.gradle.intellij.plugin})
   */
  @Override
  public void apply(Project project) {
    SarosIntellijExtension sarosExtension =
        project.getExtensions().create(EXTENSION_NAME, SarosIntellijExtension.class);
    project.afterEvaluate(p -> configureIntellijAfterEvaluate(p, sarosExtension));
    // apply gradle-intellij-plugin
    project.getPluginManager().apply(IntelliJPlugin.class);
  }

  /**
   * Method that applies the configurations which are configured in the build.gradle file. This
   * method has to be executed after the evaluation of the build files is completed. Otherwise an
   * intermediate state of the extensions are used.
   */
  private void configureIntellijAfterEvaluate(Project p, SarosIntellijExtension sarosExtension) {
    IntelliJPluginExtension intellijExtension =
        p.getExtensions().findByType(IntelliJPluginExtension.class);
    if (intellijExtension == null)
      throw new GradleException("Unable to find the extension of the intellij plugin");

    new IntellijConfigurator(p).configure(sarosExtension, intellijExtension);
  }
}
