package saros.versioning;

/** This class represents the result of a version compatibility negotiation. */
public class VersionCompatibilityResult {

  private final Compatibility compatibility;
  private Version localVersion;
  private Version remoteVersion;

  VersionCompatibilityResult(
      final Compatibility compatibility, final Version localVersion, final Version remoteVersion) {
    this.compatibility = compatibility;
    this.localVersion = localVersion;
    this.remoteVersion = remoteVersion;
  }

  /**
   * Returns the {@link Compatibility compatibility} of the negotiation result.
   *
   * @return
   */
  public Compatibility getCompatibility() {
    return compatibility;
  }

  /**
   * Returns the local version that was used for during the negotiation.
   *
   * @return
   */
  public Version getLocalVersion() {
    return localVersion;
  }

  /**
   * Returns the remote version that was used for during the negotiation.
   *
   * @return
   */
  public Version getRemoteVersion() {
    return remoteVersion;
  }
}
