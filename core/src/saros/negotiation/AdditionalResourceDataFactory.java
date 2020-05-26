package saros.negotiation;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.log4j.Logger;
import saros.filesystem.IReferencePoint;

/**
 * Factory class to build the additional project data used for the project negotiation data.
 *
 * <p>To add entries to the additional project data mapping, a {@link
 * AdditionalResourceDataProvider} can be registered. These project data providers will be called
 * with the corresponding project object every time project negotiation data is created for a shared
 * project.
 *
 * <p>The registered project data providers should provide a way of identifying their registered
 * options (e.g. by providing references to the used key values).
 *
 * <p><b>NOTE:</b> This factory does not ensure that the keys used by different project data
 * providers are disjoint. If multiple providers use the same key, the mapping of the provider added
 * last will be used.
 */
// TODO remove as no longer used?
public class AdditionalResourceDataFactory {
  private static final Logger log = Logger.getLogger(AdditionalResourceDataFactory.class);

  /** The held list of project data providers used to build the additional project data. */
  private final List<AdditionalResourceDataProvider> additionalResourceDataProviders;

  /**
   * This class should only be instantiated by the core application context. This constructor must
   * not be called directly. Instead, request the object from the plugin context.
   */
  public AdditionalResourceDataFactory() {
    this.additionalResourceDataProviders = new CopyOnWriteArrayList<>();
  }

  /**
   * Adds the passed project data providers to the list of providers used to generate the additional
   * project data for the project negotiation data.
   *
   * @param additionalResourceDataProvider the project data provider to add
   */
  public void registerProjectDataProvider(
      AdditionalResourceDataProvider additionalResourceDataProvider) {
    if (additionalResourceDataProvider != null) {
      additionalResourceDataProviders.add(additionalResourceDataProvider);
    }
  }

  /**
   * Builds the additional project data for the passed projects.
   *
   * @param project the project to build the additional project data for
   * @return the additional project data for the passed project
   */
  Map<String, String> build(IReferencePoint project) {
    Map<String, String> additionalProjectData = new HashMap<>();

    for (AdditionalResourceDataProvider additionalResourceDataProvider :
        additionalResourceDataProviders) {
      Map<String, String> providerData = additionalResourceDataProvider.getMapping(project);

      if (!Collections.disjoint(additionalProjectData.keySet(), providerData.keySet())) {
        log.warn(
            "Key sets used by project data providers are not disjoint! Noticed while processing "
                + additionalResourceDataProvider.getClass().getSimpleName());
      }

      additionalProjectData.putAll(providerData);
    }

    return additionalProjectData;
  }

  /** A class used to provide additional project data for the project negotiation. */
  public interface AdditionalResourceDataProvider {

    /**
     * Returns the mapping of additional project options for the given project.
     *
     * @param project the project to provide additional project options for
     * @return the mapping of additional project options for the given project
     */
    Map<String, String> getMapping(IReferencePoint project);
  }
}
