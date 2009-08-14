package de.fu_berlin.inf.dpp.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.XMPPChatReceiver;
import de.fu_berlin.inf.dpp.net.internal.XMPPChatTransmitter;
import de.fu_berlin.inf.dpp.net.internal.XStreamExtensionProvider;
import de.fu_berlin.inf.dpp.net.internal.XStreamExtensionProvider.XStreamIQPacket;

/**
 * Component for figuring out whether two Saros plug-in instances with known
 * Bundle Version strings are compatible.
 * 
 * This class does not use a {@link Comparator#compare(Object, Object)}, because
 * results might not be symmetrical (we only note whether current Version is A
 * is compatible with older versions, but not whether the older versions from
 * their perspective are compatible with us) and transitive (if Version A is too
 * old for B, Version B too old for C, then A might be still OK for C).
 */
@Component(module = "misc")
public class VersionManager {

    private static final Logger log = Logger.getLogger(VersionManager.class
        .getName());

    /**
     * Data Object for sending version information
     */
    public static class VersionInfo {

        public Version version;

        public Compatibility compatibility;

    }

    /**
     * Enumeration to describe whether a local version is compatible with a
     * remote one.
     */
    public enum Compatibility {
        /**
         * Versions are (probably) compatible
         */
        OK,
        /**
         * The local version is (probably) too old to work with the remote
         * version.
         * 
         * The user should be told to upgrade
         */
        TOO_OLD,
        /**
         * The local version is (probably) too new to work with the remote
         * version.
         * 
         * The user should be told to tell the peer to update.
         */
        TOO_NEW;

        /**
         * Given a result from {@link Comparator#compare(Object, Object)} will
         * return the associated Compatibility object
         */
        public static Compatibility valueOf(int comparison) {
            switch (Integer.signum(comparison)) {
            case -1:
                return TOO_OLD;
            case 0:
                return OK;
            case 1:
            default:
                return TOO_NEW;
            }
        }
    }

    /**
     * The compatibilityChart should contain for each version the list of all
     * versions which should be compatible with the given one. If no entry
     * exists for the version run by a user, the VersionManager will only return
     * {@link Compatibility#OK} if and only if the version information are
     * {@link Version#equals(Object)} to each other.
     */
    public static Map<Version, List<Version>> compatibilityChart = new HashMap<Version, List<Version>>();
    static {
        compatibilityChart.put(new Version("9.8.21.DEVEL"), Arrays
            .asList(new Version("9.8.21.DEVEL")));
    }

    /**
     * @Inject
     */
    protected Bundle bundle;

    /**
     * @Inject
     */
    protected Saros saros;

    /**
     * @Inject
     */
    protected XMPPChatTransmitter transmitter;

    protected XStreamExtensionProvider<VersionInfo> versionProvider = new XStreamExtensionProvider<VersionInfo>(
        "sarosVersion", VersionInfo.class, Version.class, Compatibility.class);

    public VersionManager(Bundle bundle, final Saros saros,
        final XMPPChatReceiver receiver, XMPPChatTransmitter transmitter) {

        this.bundle = bundle;
        this.saros = saros;
        this.transmitter = transmitter;

        receiver.addPacketListener(new PacketListener() {
            public void processPacket(Packet packet) {
                @SuppressWarnings("unchecked")
                XStreamIQPacket<VersionInfo> iq = (XStreamIQPacket<VersionInfo>) packet;

                if (iq.getType() == IQ.Type.GET) {
                    VersionInfo remote = iq.getPayload();

                    VersionInfo local = new VersionInfo();
                    local.version = getVersion();
                    local.compatibility = determineCompatbility(local.version,
                        remote.version);

                    IQ reply = versionProvider.createIQ(local);
                    reply.setType(IQ.Type.RESULT);
                    reply.setPacketID(iq.getPacketID());
                    reply.setTo(iq.getFrom());
                    saros.getConnection().sendPacket(reply);
                }
            }
        }, versionProvider.getIQFilter());
    }

    /**
     * Will query the given user for his Version and whether s/he thinks that
     * her/his remote version is compatible with our local version.
     * 
     * The resulting VersionInfo represents what the remote peer knows about
     * compatibility with our version.
     * 
     * If the resulting {@link VersionInfo#compatibility} is TOO_NEW (meaning
     * the remote version is too new), then the remote peer does not know
     * whether his version is compatible with ours. We must then check by
     * ourselves.
     * 
     * @blocking If the request times out (5s) or an error occurs null is
     *           returned.
     */
    public VersionInfo queryVersion(JID rqJID) {
        return transmitter.sendQuery(rqJID, versionProvider, 5000);
    }

    /**
     * Returns the Version of the locally running Saros plugin
     */
    public Version getVersion() {
        return Util.getBundleVersion(bundle);
    }

    /**
     * Will compare the two given Versions for compatibility. The result
     * indicates whether the localVersion passed first is compatible with the
     * remoteVersion.
     */
    public Compatibility determineCompatbility(Version localVersion,
        Version remoteVersion) {

        // If localVersion is older than remote version, then we cannot know
        // whether we are compatible
        if (localVersion.compareTo(remoteVersion) < 0) {
            return Compatibility.TOO_OLD;
        }
        List<Version> compatibleVersions = compatibilityChart.get(localVersion);
        if (compatibleVersions == null) {
            log.error("VersionManager does not know about current version:"
                + localVersion);

            // Fallback to comparing versions directly
            return Compatibility.valueOf(localVersion.compareTo(remoteVersion));
        }

        if (compatibleVersions.contains(remoteVersion))
            return Compatibility.OK;
        else
            return Compatibility.TOO_OLD;
    }

    /**
     * Given the bundle version string of a remote Saros instance, will return
     * whether the local version is compatible with the given instance.
     */
    public Compatibility determineCompatibility(String remoteVersionString) {

        Version remoteVersion = Util.parseBundleVersion(remoteVersionString);
        Version localVersion = Util.getBundleVersion(bundle);

        return determineCompatbility(localVersion, remoteVersion);
    }
}
