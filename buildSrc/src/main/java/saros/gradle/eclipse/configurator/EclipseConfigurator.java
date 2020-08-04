package saros.gradle.eclipse.configurator;

import org.gradle.api.Project;
import org.gradle.plugins.ide.eclipse.model.EclipseModel;

/** Task that configures the eclipse task which is provided by the gradle plugin 'eclipse'. */
public class EclipseConfigurator {
  private static final String PDE_NATURE_ID = "org.eclipse.pde.PluginNature";
  private static final String ECLIPSE_PLUGIN_ID = "eclipse";

  private EclipseModel eclipseModel;

  public EclipseConfigurator(Project project) {
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
}
