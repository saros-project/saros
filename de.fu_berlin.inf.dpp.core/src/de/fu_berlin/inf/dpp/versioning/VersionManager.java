package de.fu_berlin.inf.dpp.versioning;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.communication.extensions.VersionExchangeExtension;
import de.fu_berlin.inf.dpp.context.IContextKeyBindings.SarosVersion;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.PacketCollector;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;

/**
 * Component for figuring out whether two Saros plug-in instances with known Version are compatible.
 *
 * <p>This class does not use a {@link Comparator#compare(Object, Object)}, because results might
 * not be symmetrical (we only note whether current Version is A is compatible with older versions,
 * but not whether the older versions from their perspective are compatible with us) and transitive
 * (if Version A is too old for B, Version B too old for C, then A might be still OK for C).
 */
@Component(module = "core")
public class VersionManager {

  private static final String VERSION_KEY = "version";
  private static final String COMPATIBILITY_KEY = "compatibility";
  private static final String ID_KEY = "id";

  private static final Random ID_GENERATOR = new Random();

  private static final Logger LOG = Logger.getLogger(VersionManager.class);

  /**
   * The compatibilityChart should contain for each version the list of all versions which should be
   * compatible with the given one. If no entry exists for the version run by a user, the
   * VersionManager will only return {@link Compatibility#OK} if and only if the version information
   * are {@link Version#equals(Object)} to each other.
   */
  private volatile Map<Version, List<Version>> compatibilityChart =
      new HashMap<Version, List<Version>>();

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

          LOG.debug("received version request from " + packet.getFrom());

          VersionExchangeExtension versionExchangeRequest =
              VersionExchangeExtension.PROVIDER.getPayload(packet);

          if (versionExchangeRequest == null) {
            LOG.warn("cannot reply to version request, packet is malformed");
            return;
          }

          VersionExchangeExtension versionExchangeResponse = new VersionExchangeExtension();

          createResponseData:
          {
            versionExchangeResponse.set(VERSION_KEY, localVersion.toString());
            versionExchangeResponse.set(
                COMPATIBILITY_KEY, String.valueOf(Compatibility.UNKNOWN.getCode()));

            String remoteVersionString = versionExchangeRequest.get(VERSION_KEY);

            if (remoteVersionString == null) {
              LOG.warn("remote version string not found in version exchange data");
              break createResponseData;
            }

            Version remoteVersion = Version.parseVersion(remoteVersionString);

            if (remoteVersion == Version.INVALID) {
              LOG.warn("remote version string is invalid: " + remoteVersionString);
              break createResponseData;
            }

            versionExchangeResponse.set(
                COMPATIBILITY_KEY,
                String.valueOf(determineCompatibility(localVersion, remoteVersion).getCode()));

            versionExchangeResponse.set(ID_KEY, versionExchangeRequest.get(ID_KEY));
          }

          IQ reply = VersionExchangeExtension.PROVIDER.createIQ(versionExchangeResponse);
          reply.setType(IQ.Type.RESULT);
          reply.setTo(packet.getFrom());

          try {
            transmitter.sendPacket(reply);
          } catch (IOException e) {
            LOG.error("could not send version response to " + packet.getFrom(), e);
          }

          LOG.debug("send version response to " + packet.getFrom());
        }
      };

  public VersionManager(
      @SarosVersion String version, final IReceiver receiver, final ITransmitter transmitter) {

    this.localVersion = Version.parseVersion(version);

    if (this.localVersion == Version.INVALID)
      throw new IllegalArgumentException("version string is malformed: " + version);

    setCompatibilityChart(null);

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

    Compatibility remoteCompatibility = Compatibility.UNKNOWN;
    Compatibility compatibility = Compatibility.UNKNOWN;
    Version remoteVersion = Version.INVALID;

    determineCompatibility:
    {
      String remoteVersionString = versionExchangeResponse.get(VERSION_KEY);

      if (remoteVersionString == null) {
        LOG.warn("remote version string not found in version exchange data");
        break determineCompatibility;
      }

      remoteVersion = Version.parseVersion(remoteVersionString);

      if (remoteVersion == Version.INVALID) {
        LOG.warn("remote version string is invalid: " + remoteVersionString);
        break determineCompatibility;
      }

      String compatibilityString = versionExchangeResponse.get(COMPATIBILITY_KEY);

      if (compatibilityString == null) {
        LOG.warn("remote compatibility string not found in version exchange data");
        break determineCompatibility;
      }

      try {
        remoteCompatibility = Compatibility.fromCode(Integer.valueOf(compatibilityString));
      } catch (NumberFormatException e) {
        LOG.warn("remote compatibility string contains non numerical characters");
      }

      compatibility = determineCompatibility(localVersion, remoteVersion);

      // believe what the remote side told us in case we are too old
      if (compatibility == Compatibility.TOO_OLD && remoteCompatibility == Compatibility.OK)
        compatibility = Compatibility.OK;
    }

    return new VersionCompatibilityResult(compatibility, localVersion, remoteVersion);
  }

  /**
   * Sets an compatibility char that contains additional version information. The chart should be
   * loaded from a property file which must use the following syntax:
   *
   * <pre>
   *     <tt>local_version = remote_version { & remote_version }</tt>
   * </pre>
   *
   * @param chart the chart to set or <code>null</code> to reset the compatibility chart
   */
  public void setCompatibilityChart(Properties chart) {

    final Map<Version, List<Version>> newCompatibilityChart = new HashMap<Version, List<Version>>();

    if (chart == null) chart = new Properties(); // dummy

    for (final Object versionKey : chart.keySet()) {
      final Version version = Version.parseVersion(versionKey.toString());

      if (version == Version.INVALID) continue;

      final List<Version> compatibleVersions = new ArrayList<Version>();

      for (final String compatibleVersionString : chart.get(versionKey).toString().split("&")) {

        final Version compatibleVersion = Version.parseVersion(compatibleVersionString.trim());

        if (compatibleVersion != Version.INVALID) compatibleVersions.add(compatibleVersion);
      }

      if (!compatibleVersions.contains(version)) compatibleVersions.add(version);

      newCompatibilityChart.put(version, compatibleVersions);
    }

    final Version currentVersion = localVersion;

    List<Version> currentCompatibleVersions = newCompatibilityChart.get(currentVersion);

    if (currentCompatibleVersions == null) {
      currentCompatibleVersions = new ArrayList<Version>();
      newCompatibilityChart.put(currentVersion, currentCompatibleVersions);
    }

    if (!currentCompatibleVersions.contains(currentVersion))
      currentCompatibleVersions.add(currentVersion);

    if (LOG.isTraceEnabled()) {
      LOG.trace("current version compatibility chart: " + newCompatibilityChart);
    }

    this.compatibilityChart = newCompatibilityChart;
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
      LOG.warn(e.getMessage(), e);
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

    final Map<Version, List<Version>> currentChart = compatibilityChart;

    // remote version is lower than our version
    if (compatibility == Compatibility.TOO_NEW && currentChart != null) {

      List<Version> compatibleVersions = currentChart.get(localVersion);

      assert compatibleVersions != null;

      if (compatibleVersions.contains(remoteVersion)) compatibility = Compatibility.OK;
    }

    return compatibility;
  }
}
