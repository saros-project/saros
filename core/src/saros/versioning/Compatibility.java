package saros.versioning;

/** Enumeration to describe whether a local version is compatible with a remote one. */
public enum Compatibility {

  /** Versions are (probably) compatible */
  OK,

  /**
   * The local version is (probably) too old to work with the remote version.
   *
   * <p>The user should be told to upgrade.
   */
  OLDER,

  /**
   * The local version is (probably) too new to work with the remote version.
   *
   * <p>The user should be told to tell the peer to update.
   */
  NEWER,

  /**
   * At least one of the versions contains a qualifier and the complete version string does not
   * match.
   */
  QUALIFIER_MISMATCH,

  /** The two versions specified different, incompatible Saros implementations. */
  INCOMPATIBLE_IMPLEMENTATIONS,

  /** The compatibility could not be determined. */
  UNKNOWN
}
