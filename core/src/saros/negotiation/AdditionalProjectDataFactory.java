package saros.negotiation;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.log4j.Logger;
import saros.filesystem.IProject;

/**
 * Factory class to build the additional project data used for the project negotiation data.
 *
 * <p>To add entries to the additional project data mapping, a {@link ProjectDataProvider} can be
 * registered. These project data providers will be called with the corresponding project object
 * every time project negotiation data is created for a shared project.
 *
 * <p>The registered project data providers should provide a way of identifying their registered
 * options (e.g. by providing references to the used key values).
 *
 * <p><b>NOTE:</b> This factory does not ensure that the keys used by different project data
 * providers are disjoint. If multiple providers use the same key, the mapping of the provider added
 * last will be used.
 */
public class AdditionalProjectDataFactory {
  private static final Logger log = Logger.getLogger(AdditionalProjectDataFactory.class);

  /** The held list of project data providers used to build the additional project data. */
  private final List<ProjectDataProvider> projectDataProviders;

  /**
   * This class should only be instantiated by the core application context. This constructor must
   * not be called directly. Instead, request the object from the plugin context.
   */
  public AdditionalProjectDataFactory() {
    this.projectDataProviders = new CopyOnWriteArrayList<>();
  }

  /**
   * Adds the passed project data providers to the list of providers used to generate the additional
   * project data for the project negotiation data.
   *
   * @param projectDataProvider the project data provider to add
   */
  public void registerProjectDataProvider(ProjectDataProvider projectDataProvider) {
    if (projectDataProvider != null) {
      projectDataProviders.add(projectDataProvider);
    }
  }

  /**
   * Builds the additional project data for the passed projects.
   *
   * @param project the project to build the additional project data for
   * @return the additional project data for the passed project
   */
  Map<String, String> build(IProject project) {
    Map<String, String> additionalProjectData = new HashMap<>();

    for (ProjectDataProvider projectDataProvider : projectDataProviders) {
      Map<String, String> providerData = projectDataProvider.getMapping(project);

      if (!Collections.disjoint(additionalProjectData.keySet(), providerData.keySet())) {
        log.warn(
            "Key sets used by project data providers are not disjoint! Noticed while processing "
                + projectDataProvider.getClass().getSimpleName());
      }

      additionalProjectData.putAll(providerData);
    }

    return additionalProjectData;
  }

  /** A class used to provide additional project data for the project negotiation. */
  public interface ProjectDataProvider {

    /**
     * Returns the mapping of additional project options for the given project.
     *
     * @param project the project to provide additional project options for
     * @return the mapping of additional project options for the given project
     */
    Map<String, String> getMapping(IProject project);
  }
}
