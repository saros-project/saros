package saros.negotiation;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.log4j.Logger;
import saros.filesystem.IReferencePoint;

/**
 * Factory class to build the additional resource data used for the resource negotiation data.
 *
 * <p>To add entries to the additional resource data mapping, a {@link
 * AdditionalResourceDataProvider} can be registered. These additional resource data providers will
 * be called with the corresponding reference point object every time resource negotiation data is
 * created for a shared reference point.
 *
 * <p>The registered additional resource data providers should provide a way of identifying their
 * registered options (e.g. by providing references to the used key values).
 *
 * <p><b>NOTE:</b> This factory does not ensure that the keys used by different additional resource
 * data providers are disjoint. If multiple providers use the same key, the mapping of the provider
 * added last will be used.
 */
// TODO remove as no longer used?
public class AdditionalResourceDataFactory {
  private static final Logger log = Logger.getLogger(AdditionalResourceDataFactory.class);

  /**
   * The held list of additional resource data providers used to build the additional resource data.
   */
  private final List<AdditionalResourceDataProvider> additionalResourceDataProviders;

  /**
   * This class should only be instantiated by the core application context. This constructor must
   * not be called directly. Instead, request the object from the plugin context.
   */
  public AdditionalResourceDataFactory() {
    this.additionalResourceDataProviders = new CopyOnWriteArrayList<>();
  }

  /**
   * Adds the passed additional resource data providers to the list of providers used to generate
   * the additional resource data for the resource negotiation data.
   *
   * @param additionalResourceDataProvider the additional resource data provider to add
   */
  public void registerAdditionalResourceDataProvider(
      AdditionalResourceDataProvider additionalResourceDataProvider) {

    if (additionalResourceDataProvider != null) {
      additionalResourceDataProviders.add(additionalResourceDataProvider);
    }
  }

  /**
   * Builds the additional resource data for the passed reference point.
   *
   * @param referencePoint the reference point to build the additional resource data for
   * @return the additional resource data for the passed reference point
   */
  Map<String, String> build(IReferencePoint referencePoint) {
    Map<String, String> additionalResourceData = new HashMap<>();

    for (AdditionalResourceDataProvider additionalResourceDataProvider :
        additionalResourceDataProviders) {
      Map<String, String> providerData = additionalResourceDataProvider.getMapping(referencePoint);

      if (!Collections.disjoint(additionalResourceData.keySet(), providerData.keySet())) {
        log.warn(
            "Key sets used by additional resource data providers are not disjoint! Noticed while"
                + " processing "
                + additionalResourceDataProvider.getClass().getSimpleName());
      }

      additionalResourceData.putAll(providerData);
    }

    return additionalResourceData;
  }

  /** A class used to provide additional resource data for the resource negotiation. */
  public interface AdditionalResourceDataProvider {

    /**
     * Returns the mapping of additional resource data for the given reference point.
     *
     * @param referencePoint the reference point to provide additional resource data for
     * @return the mapping of additional resource data for the given reference point
     */
    Map<String, String> getMapping(IReferencePoint referencePoint);
  }
}
