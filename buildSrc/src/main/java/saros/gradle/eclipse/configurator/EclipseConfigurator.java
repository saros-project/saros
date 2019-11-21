package saros.gradle.eclipse.configurator;

import java.util.HashMap;
import org.gradle.api.Project;
import org.gradle.api.XmlProvider;
import org.gradle.plugins.ide.api.XmlFileContentMerger;
import org.gradle.plugins.ide.eclipse.model.EclipseModel;

/** Task that configures the eclipse task which is provided by the gradle plugin 'eclipse'. */
public class EclipseConfigurator {
  private static final String PDE_NATURE_ID = "org.eclipse.pde.PluginNature";
  private static final String ECLIPSE_PLUGIN_ID = "eclipse";

  // gradle project name of the relocated picocontainer project
  private static final String PICOCONTAINER_PROJECT_NAME = ":saros.picocontainer";
  // configuration of the picocontainer project that references the relocated jar
  private static final String PICOCONTAINER_CONFIG_NAME = "shadow";

  private EclipseModel eclipseModel;
  private Project project;

  public EclipseConfigurator(Project project) {
    this.project = project;
    project.getPluginManager().apply(ECLIPSE_PLUGIN_ID);
    this.eclipseModel = project.getExtensions().findByType(EclipseModel.class);
  }

  /**
   * Adds the pde nature to an eclipse model. This leads to the generation of eclispe meta files
   * which allow us to use the eclipse pde tools on the corresponding project.
   */
  public void addPdeNature() {
    eclipseModel.getProject().natures(PDE_NATURE_ID);
  }

  /**
   * We relocate (change the package naming) the picocontainer library in order to avoid issues with
   * the picocontainer version shipped with Intellij.
   */
  public void usePatchedPicocontainer() {
    getEclipseXmlContentMerger()
        .withXml(
            (XmlProvider xmlProvider) -> {
              // Add new classpath entry to include the relocated picocontainer
              String relocatedPicoContainerJarPath = getRelocatedPicoContainerJarPath();
              addNewLibToClasspath(relocatedPicoContainerJarPath, xmlProvider);
            });
  }

  /**
   * Adds a library to the eclipse classpath. This method simply adds the library to the general
   * context main and test.
   *
   * @param libraryPath The path to the library that will be added
   * @param provider The provider of the classpath file
   */
  private void addNewLibToClasspath(String libraryPath, XmlProvider provider) {
    provider
        .asNode()
        .appendNode(
            "classpathentry",
            new HashMap<String, String>() {
              {
                put("kind", "lib");
                put("path", libraryPath);
              }
            })
        .appendNode(
            "attribute",
            new HashMap<String, String>() {
              {
                put("name", "gradle_used_by_scope");
                put("value", "main,test");
              }
            });
  }

  private String getRelocatedPicoContainerJarPath() {
    // Use just the first artifact of the configuration, because we know that we only
    // provide one artifact: The relocated jar.
    return project
        .project(PICOCONTAINER_PROJECT_NAME)
        .getConfigurations()
        .getByName(PICOCONTAINER_CONFIG_NAME)
        .getArtifacts()
        .iterator()
        .next()
        .getFile()
        .getAbsolutePath();
  }

  private XmlFileContentMerger getEclipseXmlContentMerger() {
    return eclipseModel.getClasspath().getFile();
  }
}
