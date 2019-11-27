package saros.gradle.eclipse.configurator;

import java.io.File;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.file.CopySpec;
import org.gradle.api.java.archives.Manifest;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.jvm.tasks.Jar;

public class JarConfigurator {

  private static final String JAVA_MAIN_SOURCE_SET_NAME = "main";
  private static final String JAVA_PLUGIN_ID = "java";
  private static final String JAR_TASK_NAME = "jar";

  private static final String RELEASE_CONFIG_NAME = "releaseDep";
  private static final String JAR_LIB_DESTINATION = "lib";

  private Project project;

  public JarConfigurator(Project project) {
    this.project = project;
    project.getPluginManager().apply(JAVA_PLUGIN_ID);
  }

  public void createBundleJar(File manifestFile) {
    Jar jarTask = (Jar) project.getTasks().findByName(JAR_TASK_NAME);

    if (jarTask == null)
      throw new GradleException("Unable to find the gradle task: " + JAR_TASK_NAME);

    jarTask.manifest((Manifest mf) -> mf.from(manifestFile));
    SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
    jarTask.from(sourceSets.getByName(JAVA_MAIN_SOURCE_SET_NAME).getOutput());
    jarTask.into(
        JAR_LIB_DESTINATION,
        (CopySpec cs) -> cs.from(project.getConfigurations().getByName(RELEASE_CONFIG_NAME)));
  }
}
