package saros.net;

import java.util.Optional;

/** Identifier of Saros Resource Features */
public enum ResourceFeature {
  /** Saros Support */
  SAROS("saros");

  private final String identifier;

  private ResourceFeature(String identifier) {
    this.identifier = identifier;
  }

  public String getIdentifier() {
    return identifier;
  }

  /**
   * Returns the corresponding enum if the identifier is known.
   *
   * @param identifier Search String
   * @return Optional with ResourceFeature if found
   */
  public static Optional<ResourceFeature> getFeature(String identifier) {
    for (ResourceFeature feature : values()) {
      if (feature.identifier.equals(identifier)) return Optional.of(feature);
    }
    return Optional.empty();
  }
}
