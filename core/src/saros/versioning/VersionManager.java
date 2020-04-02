package saros.versioning;

import java.util.Objects;
import org.apache.log4j.Logger;
import saros.annotations.Component;
import saros.communication.InfoManager;
import saros.context.IContextKeyBindings.SarosVersion;
import saros.net.xmpp.contact.XMPPContact;

/**
 * Component for figuring out whether two Saros plug-in instances with known Version are compatible.
 *
 * <p>This class compares if local and remote version (not checking qualifier) are the same.
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

  public VersionManager(@SarosVersion String version, InfoManager infoManager) {
    this.localVersion = Version.parseVersion(version);
    if (this.localVersion == Version.INVALID)
      throw new IllegalArgumentException("version string is malformed: " + version);

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

    Compatibility compatibility = determineCompatibility(localVersion, remoteVersion);
    return new VersionCompatibilityResult(compatibility, localVersion, remoteVersion);
  }

  /**
   * Compares the two given versions for compatibility. The result indicates whether the local
   * version is compatible with the remote version.
   */
  private Compatibility determineCompatibility(Version localVersion, Version remoteVersion) {
    Compatibility compatibility = Compatibility.valueOf(localVersion.compareTo(remoteVersion));

    return compatibility;
  }
}
