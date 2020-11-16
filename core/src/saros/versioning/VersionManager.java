package saros.versioning;

import java.util.Objects;
import org.apache.log4j.Logger;
import saros.annotations.Component;
import saros.communication.InfoManager;
import saros.context.IContextKeyBindings.SarosImplementation;
import saros.context.IContextKeyBindings.SarosVersion;
import saros.net.xmpp.contact.XMPPContact;

/**
 * Component for figuring out whether two Saros plug-in instances with known Version are compatible.
 *
 * @see Version#determineCompatibilityWith(Version)
 */
@Component(module = "core")
public class VersionManager {
  /* If you want to implement backward compatibility in a later version, as a suggestion:
   * acknowledge same major.minor version as compatible. Alternatively add a `backwards_compatibility`
   * key with the last working version as value and add a check for it.
   */

  public static final String VERSION_KEY = "version";

  private static final Logger log = Logger.getLogger(VersionManager.class);

  private final Version localVersion;
  private final InfoManager infoManager;

  public VersionManager(
      @SarosImplementation String implementation,
      @SarosVersion String version,
      InfoManager infoManager) {

    this.localVersion = Version.parseVersion(implementation, version);

    if (this.localVersion == Version.INVALID) {
      throw new IllegalArgumentException("version string is malformed: " + version);
    }

    this.infoManager = infoManager;
    infoManager.setLocalInfo(VERSION_KEY, localVersion.toString());
  }

  /**
   * Determines the version compatibility with the given contact.
   *
   * @param contact the remote party
   * @return the {@link VersionCompatibilityResult VersionCompatibilityResult}
   */
  public VersionCompatibilityResult determineVersionCompatibility(XMPPContact contact) {
    Objects.requireNonNull(contact, "contact is needed");

    String versionString = infoManager.getRemoteInfo(contact, VERSION_KEY).orElse(null);

    if (versionString == null) {
      log.warn("remote version not found for: " + contact);
      return new VersionCompatibilityResult(Compatibility.UNKNOWN, localVersion, Version.INVALID);
    }

    Version remoteVersion = Version.parseVersion(versionString);

    if (remoteVersion == Version.INVALID) {
      log.warn("contact: " + contact + ", remote version string is invalid: " + versionString);
    }

    Compatibility compatibility = localVersion.determineCompatibilityWith(remoteVersion);
    return new VersionCompatibilityResult(compatibility, localVersion, remoteVersion);
  }
}
