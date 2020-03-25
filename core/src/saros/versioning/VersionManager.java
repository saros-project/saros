package saros.versioning;

import java.io.IOException;
import java.util.Random;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import saros.annotations.Component;
import saros.communication.extensions.VersionExchangeExtension;
import saros.context.IContextKeyBindings.SarosVersion;
import saros.net.IReceiver;
import saros.net.ITransmitter;
import saros.net.PacketCollector;
import saros.net.xmpp.JID;

/**
 * Component for figuring out whether two Saros plug-in instances with known Version are compatible.
 *
 * <p>This class compares if local and remote version (not checking qualifier) are the same.
 *
 * <p>If you want to implement backward compatibility in a later version, as a suggestion:
 * acknowledge same major.minor version as compatible. Alternatively add a `backwards_compatibility`
 * key with the last working version as value and add a check for it.
 */
@Component(module = "core")
public class VersionManager {

  private static final String VERSION_KEY = "version";
  private static final String ID_KEY = "id";

  private static final Random ID_GENERATOR = new Random();

  private static final Logger log = Logger.getLogger(VersionManager.class);

  private final Version localVersion;
  private final ITransmitter transmitter;
  private final IReceiver receiver;

  private final PacketListener versionRequestListener =
      new PacketListener() {
        /*
         * As some of the logic may changed in the next Saros versions this
         * method MUST be robust as possible, expect weird and faulty data !
         */
        @Override
        public void processPacket(Packet packet) {

          log.debug("received version request from " + packet.getFrom());

          VersionExchangeExtension versionExchangeRequest =
              VersionExchangeExtension.PROVIDER.getPayload(packet);

          if (versionExchangeRequest == null) {
            log.warn("cannot reply to version request, packet is malformed");
            return;
          }

          VersionExchangeExtension versionExchangeResponse = new VersionExchangeExtension();

          createResponseData:
          {
            versionExchangeResponse.set(VERSION_KEY, localVersion.toString());

            String remoteVersionString = versionExchangeRequest.get(VERSION_KEY);
            if (remoteVersionString == null) {
              log.warn("remote version string not found in version exchange data");
              break createResponseData;
            }

            Version remoteVersion = Version.parseVersion(remoteVersionString);
            if (remoteVersion == Version.INVALID) {
              log.warn("remote version string is invalid: " + remoteVersionString);
              break createResponseData;
            }

            versionExchangeResponse.set(ID_KEY, versionExchangeRequest.get(ID_KEY));
          }

          IQ reply = VersionExchangeExtension.PROVIDER.createIQ(versionExchangeResponse);
          reply.setType(IQ.Type.RESULT);
          reply.setTo(packet.getFrom());

          try {
            transmitter.sendPacket(reply);
          } catch (IOException e) {
            log.error("could not send version response to " + packet.getFrom(), e);
          }

          log.debug("send version response to " + packet.getFrom());
        }
      };

  public VersionManager(
      @SarosVersion String version, final IReceiver receiver, final ITransmitter transmitter) {

    this.localVersion = Version.parseVersion(version);

    if (this.localVersion == Version.INVALID)
      throw new IllegalArgumentException("version string is malformed: " + version);

    this.receiver = receiver;
    this.transmitter = transmitter;

    receiver.addPacketListener(
        versionRequestListener,
        new AndFilter(
            VersionExchangeExtension.PROVIDER.getIQFilter(),
            new PacketFilter() {
              @Override
              public boolean accept(Packet packet) {
                return ((IQ) packet).getType() == IQ.Type.GET;
              }
            }));
  }

  /**
   * Determines the version compatibility with the given peer.
   *
   * @param rqJID the resource qualified JID of the peer
   * @return the compatibility {@link VersionCompatibilityResult result} of the version
   *     compatibility negotiation or <code>null</code> if the remote side did not replied
   * @blocking this method may block several seconds up until the internal configured timeout is
   *     reached
   */
  /*
   * As some of the logic may changed in the next Saros versions this method
   * MUST be robust as possible, expect weird and faulty data !
   */

  public VersionCompatibilityResult determineVersionCompatibility(final JID rqJID) {
    VersionExchangeExtension versionExchangeResponse = queryRemoteVersionDetails(rqJID, 10000);

    if (versionExchangeResponse == null) return null;

    Compatibility compatibility = Compatibility.UNKNOWN;
    Version remoteVersion = Version.INVALID;

    determineCompatibility:
    {
      String remoteVersionString = versionExchangeResponse.get(VERSION_KEY);
      if (remoteVersionString == null) {
        log.warn("remote version string not found in version exchange data");
        break determineCompatibility;
      }

      remoteVersion = Version.parseVersion(remoteVersionString);
      if (remoteVersion == Version.INVALID) {
        log.warn("remote version string is invalid: " + remoteVersionString);
        break determineCompatibility;
      }

      compatibility = determineCompatibility(localVersion, remoteVersion);
    }

    return new VersionCompatibilityResult(compatibility, localVersion, remoteVersion);
  }

  private VersionExchangeExtension queryRemoteVersionDetails(final JID rqJID, final long timeout) {
    assert rqJID.isResourceQualifiedJID();

    final String exchangeID = String.valueOf(ID_GENERATOR.nextInt());
    VersionExchangeExtension versionExchangeRequest = new VersionExchangeExtension();

    versionExchangeRequest.set(VERSION_KEY, localVersion.toString());
    versionExchangeRequest.set(ID_KEY, String.valueOf(exchangeID));

    IQ request = VersionExchangeExtension.PROVIDER.createIQ(versionExchangeRequest);

    request.setType(IQ.Type.GET);
    request.setTo(rqJID.toString());

    PacketCollector collector =
        receiver.createCollector(
            new AndFilter(
                VersionExchangeExtension.PROVIDER.getIQFilter(),
                new PacketFilter() {
                  @Override
                  public boolean accept(Packet packet) {

                    VersionExchangeExtension versionExchange =
                        VersionExchangeExtension.PROVIDER.getPayload(packet);

                    if (versionExchange == null) return false;

                    return rqJID.toString().equals(packet.getFrom())
                        && ((IQ) packet).getType() == IQ.Type.RESULT
                        && exchangeID.equals(versionExchange.get(ID_KEY));
                  }
                }));

    try {
      transmitter.sendPacket(request);
      return VersionExchangeExtension.PROVIDER.getPayload(collector.nextResult(timeout));
    } catch (IOException e) {
      log.warn(e.getMessage(), e);
      return null;
    } finally {
      collector.cancel();
    }
  }

  /**
   * Compares the two given versions for compatibility. The result indicates whether the local
   * version is compatible with the remote version.
   */

  // package protected only for testing purposes !
  Compatibility determineCompatibility(Version localVersion, Version remoteVersion) {
    Compatibility compatibility = Compatibility.valueOf(localVersion.compareTo(remoteVersion));

    return compatibility;
  }
}
