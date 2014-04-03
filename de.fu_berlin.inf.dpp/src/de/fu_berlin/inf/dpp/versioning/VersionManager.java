package de.fu_berlin.inf.dpp.versioning;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;

import de.fu_berlin.inf.dpp.ISarosContextBindings.SarosVersion;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.communication.extensions.VersionExchangeExtension;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.SarosPacketCollector;

/**
 * Component for figuring out whether two Saros plug-in instances with known
 * Version are compatible.
 * 
 * This class does not use a {@link Comparator#compare(Object, Object)}, because
 * results might not be symmetrical (we only note whether current Version is A
 * is compatible with older versions, but not whether the older versions from
 * their perspective are compatible with us) and transitive (if Version A is too
 * old for B, Version B too old for C, then A might be still OK for C).
 */
@Component(module = "core")
public class VersionManager {

    private static final String VERSION_KEY = "version";
    private static final String COMPATIBILITY_KEY = "compatibility";
    private static final String ID_KEY = "id";

    private static final Random ID_GENERATOR = new Random();

    private static final Logger log = Logger.getLogger(VersionManager.class);

    private static final String COMPATIBILITY_PROPERTY_FILE = "version.comp";

    /**
     * The compatibilityChart should contain for each version the list of all
     * versions which should be compatible with the given one. If no entry
     * exists for the version run by a user, the VersionManager will only return
     * {@link Compatibility#OK} if and only if the version information are
     * {@link Version#equals(Object)} to each other.
     */
    private final Map<Version, List<Version>> compatibilityChart = new HashMap<Version, List<Version>>();

    private final Version version;
    private final ITransmitter transmitter;
    private final IReceiver receiver;

    private final PacketListener versionRequestListener = new PacketListener() {
        /*
         * As some of the logic may changed in the next Saros versions this
         * method MUST be robust as possible, expect weird and faulty data !
         */
        @Override
        public void processPacket(Packet packet) {

            log.debug("received version request from " + packet.getFrom());

            VersionExchangeExtension versionExchangeRequest = VersionExchangeExtension.PROVIDER
                .getPayload(packet);

            if (versionExchangeRequest == null) {
                log.warn("cannot reply to version request, packet is malformed");
                return;
            }

            VersionExchangeExtension versionExchangeResponse = new VersionExchangeExtension();

            createResponseData: {

                versionExchangeResponse.set(VERSION_KEY, version.toString());
                versionExchangeResponse.set(COMPATIBILITY_KEY,
                    String.valueOf(Compatibility.UNKNOWN.getCode()));

                String remoteVersionString = versionExchangeRequest
                    .get(VERSION_KEY);

                if (remoteVersionString == null) {
                    log.warn("remote version string not found in version exchange data");
                    break createResponseData;
                }

                Version remoteVersion = Version
                    .parseVersion(remoteVersionString);

                if (remoteVersion == Version.INVALID) {
                    log.warn("remote version string is invalid: "
                        + remoteVersionString);
                    break createResponseData;
                }

                versionExchangeResponse.set(
                    COMPATIBILITY_KEY,
                    String.valueOf(determineCompatibility(version,
                        remoteVersion).getCode()));

                versionExchangeResponse.set(ID_KEY,
                    versionExchangeRequest.get(ID_KEY));
            }

            IQ reply = VersionExchangeExtension.PROVIDER
                .createIQ(versionExchangeResponse);
            reply.setType(IQ.Type.RESULT);
            reply.setTo(packet.getFrom());

            try {
                transmitter.sendPacket(reply);
            } catch (IOException e) {
                log.error(
                    "could not send version response to " + packet.getFrom(), e);
            }

            log.debug("send version response to " + packet.getFrom());

        }

    };

    public VersionManager(@SarosVersion String version,
        final IReceiver receiver, final ITransmitter transmitter) {

        this.version = Version.parseVersion(version);

        if (this.version == Version.INVALID)
            throw new IllegalArgumentException("version string is malformed: "
                + version);

        this.receiver = receiver;
        this.transmitter = transmitter;

        receiver.addPacketListener(versionRequestListener, new AndFilter(
            VersionExchangeExtension.PROVIDER.getIQFilter(),
            new PacketFilter() {
                @Override
                public boolean accept(Packet packet) {
                    return ((IQ) packet).getType() == IQ.Type.GET;

                }
            }));

        initializeCompatibilityChart();
    }

    /**
     * Determines the version compatibility with the given peer.
     * 
     * @param rqJID
     *            the resource qualified JID of the peer
     * 
     * @return the compatibility {@link VersionCompatibilityResult result} of
     *         the version compatibility negotiation or <code>null</code> if the
     *         remote side did not replied
     * @blocking this method may block several seconds up until the internal
     *           configured timeout is reached
     */
    /*
     * As some of the logic may changed in the next Saros versions this method
     * MUST be robust as possible, expect weird and faulty data !
     */

    public VersionCompatibilityResult determineVersionCompatibility(
        final JID rqJID) {

        VersionExchangeExtension versionExchangeResponse = queryRemoteVersionDetails(
            rqJID, 10000);

        if (versionExchangeResponse == null)
            return null;

        Compatibility remoteCompatibility = Compatibility.UNKNOWN;
        Compatibility compatibility = Compatibility.UNKNOWN;
        Version remoteVersion = Version.INVALID;

        determineCompatibility: {

            String remoteVersionString = versionExchangeResponse
                .get(VERSION_KEY);

            if (remoteVersionString == null) {
                log.warn("remote version string not found in version exchange data");
                break determineCompatibility;
            }

            remoteVersion = Version.parseVersion(remoteVersionString);

            if (remoteVersion == Version.INVALID) {
                log.warn("remote version string is invalid: "
                    + remoteVersionString);
                break determineCompatibility;
            }

            String compatibilityString = versionExchangeResponse
                .get(COMPATIBILITY_KEY);

            if (compatibilityString == null) {
                log.warn("remote compatibility string not found in version exchange data");
                break determineCompatibility;
            }

            try {
                remoteCompatibility = Compatibility.fromCode(Integer
                    .valueOf(compatibilityString));
            } catch (NumberFormatException e) {
                log.warn("remote compatibility string contains non numerical characters");
            }

            compatibility = determineCompatibility(version, remoteVersion);

            // believe what the remote side told us in case we are too old
            if (compatibility == Compatibility.TOO_OLD
                && remoteCompatibility == Compatibility.OK)
                compatibility = Compatibility.OK;
        }

        return new VersionCompatibilityResult(compatibility, version,
            remoteVersion);
    }

    private VersionExchangeExtension queryRemoteVersionDetails(final JID rqJID,
        final long timeout) {

        assert rqJID.isResourceQualifiedJID();

        final String exchangeID = String.valueOf(ID_GENERATOR.nextInt());
        VersionExchangeExtension versionExchangeRequest = new VersionExchangeExtension();

        versionExchangeRequest.set(VERSION_KEY, version.toString());
        versionExchangeRequest.set(ID_KEY, String.valueOf(exchangeID));

        IQ request = VersionExchangeExtension.PROVIDER
            .createIQ(versionExchangeRequest);

        request.setType(IQ.Type.GET);
        request.setTo(rqJID.toString());

        SarosPacketCollector collector = receiver
            .createCollector(new AndFilter(VersionExchangeExtension.PROVIDER
                .getIQFilter(), new PacketFilter() {
                @Override
                public boolean accept(Packet packet) {

                    VersionExchangeExtension versionExchange = VersionExchangeExtension.PROVIDER
                        .getPayload(packet);

                    if (versionExchange == null)
                        return false;

                    return rqJID.toString().equals(packet.getFrom())
                        && ((IQ) packet).getType() == IQ.Type.RESULT
                        && exchangeID.equals(versionExchange.get(ID_KEY));
                }
            }));

        try {
            transmitter.sendPacket(request);
            return VersionExchangeExtension.PROVIDER.getPayload(collector
                .nextResult(timeout));
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
            return null;
        } finally {
            collector.cancel();
        }
    }

    /**
     * Compares the two given versions for compatibility. The result indicates
     * whether the local version is compatible with the remote version.
     */

    // package protected only for testing purposes !
    Compatibility determineCompatibility(Version localVersion,
        Version remoteVersion) {

        Compatibility compatibility = Compatibility.valueOf(localVersion
            .compareTo(remoteVersion));

        // remote version is lower than our version
        if (compatibility == Compatibility.TOO_NEW) {

            List<Version> compatibleVersions = compatibilityChart
                .get(localVersion);

            assert compatibleVersions != null;

            if (compatibleVersions.contains(remoteVersion))
                compatibility = Compatibility.OK;
        }

        return compatibility;
    }

    private void initializeCompatibilityChart() {

        Properties properties = loadCompatibilityProperties(COMPATIBILITY_PROPERTY_FILE);

        for (Object versionKey : properties.keySet()) {
            Version version = Version.parseVersion(versionKey.toString());

            if (version == Version.INVALID)
                continue;

            List<Version> compatibleVersions = new ArrayList<Version>();

            for (String compatibleVersionString : properties.get(versionKey)
                .toString().split("&")) {

                Version compatibleVersion = Version
                    .parseVersion(compatibleVersionString.trim());

                if (compatibleVersion != Version.INVALID)
                    compatibleVersions.add(compatibleVersion);
            }

            if (!compatibleVersions.contains(version))
                compatibleVersions.add(version);

            compatibilityChart.put(version, compatibleVersions);
        }

        Version currentVersion = version;

        List<Version> currentCompatibleVersions = compatibilityChart
            .get(currentVersion);

        if (currentCompatibleVersions == null) {
            currentCompatibleVersions = new ArrayList<Version>();
            compatibilityChart.put(currentVersion, currentCompatibleVersions);
        }

        if (!currentCompatibleVersions.contains(currentVersion))
            currentCompatibleVersions.add(currentVersion);
    }

    private Properties loadCompatibilityProperties(String filename) {

        InputStream in = VersionManager.class.getClassLoader()
            .getResourceAsStream(filename);

        Properties properties = new Properties();

        if (in == null) {
            log.warn("could not find compatibility property file: " + filename);

            return properties;
        }

        try {
            properties.load(in);
        } catch (IOException e) {
            log.warn("could not read compatibility property file: " + filename,
                e);

            properties.clear();
        } finally {
            IOUtils.closeQuietly(in);
        }

        return properties;
    }
}
