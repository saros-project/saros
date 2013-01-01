package de.fu_berlin.inf.dpp.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.osgi.framework.Version;

import de.fu_berlin.inf.dpp.SarosContext.Bindings.SarosVersion;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.SarosPacketCollector;
import de.fu_berlin.inf.dpp.net.internal.extensions.XStreamExtensionProvider;
import de.fu_berlin.inf.dpp.net.internal.extensions.XStreamExtensionProvider.XStreamIQPacket;

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

    private static final Logger log = Logger.getLogger(VersionManager.class);

    private static final XStreamExtensionProvider<VersionInfo> VERSION_PROVIDER = new XStreamExtensionProvider<VersionInfo>(
        "sarosVersion", VersionInfo.class, Version.class, Compatibility.class);

    /**
     * Data Object for sending version information
     */
    public static class VersionInfo {

        public String version;

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
        OK {
            @Override
            public Compatibility invert() {
                return OK;
            }
        },
        /**
         * The local version is (probably) too old to work with the remote
         * version.
         * 
         * The user should be told to upgrade
         */
        TOO_OLD {
            @Override
            public Compatibility invert() {
                return TOO_NEW;
            }
        },
        /**
         * The local version is (probably) too new to work with the remote
         * version.
         * 
         * The user should be told to tell the peer to update.
         */
        TOO_NEW {
            @Override
            public Compatibility invert() {
                return TOO_OLD;
            }
        };

        /**
         * 
         * @return <code>TOO_OLD</code> if the initial compatibility was
         *         <code>TOO_NEW</code>, <code>TOO_NEW</code> if the initial
         *         compatibility was <code>TOO_OLD</code>, <code>OK</code>
         *         otherwise
         */
        public abstract Compatibility invert();

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
    private final Map<Version, List<Version>> COMPATIBILITY_CHART = new HashMap<Version, List<Version>>();

    /**
     * Initialize the compatibility map.
     * 
     * For each version, all older versions that are compatible should be added
     * in order of release date.
     * 
     * For the first version which is too old the commit which broke
     * compatibility should be listed.
     */
    {

        /**
         * Version 12.9.28
         */
        COMPATIBILITY_CHART.put(new Version("12.9.28"), Arrays.asList(
            new Version("12.9.28"), new Version("12.7.27"), new Version(
                "12.7.6")));

        /**
         * Version 12.7.27
         */
        COMPATIBILITY_CHART.put(new Version("12.7.27"),
            Arrays.asList(new Version("12.7.27"), new Version("12.7.6")));

        /**
         * Version 12.7.6
         */
        COMPATIBILITY_CHART.put(new Version("12.7.6"),
            Arrays.asList(new Version("12.7.6")));

        /**
         * Version 12.3.30.r3893
         */
        COMPATIBILITY_CHART.put(new Version("12.3.30.r3893"),
            Arrays.asList(new Version("12.3.30.r3893")));

        /**
         * Version 11.12.9.r3685
         */
        COMPATIBILITY_CHART.put(new Version("11.12.9.r3685"), Arrays.asList(
            new Version("11.12.9.r3685"), new Version("11.9.30.r3567"),
            new Version("11.7.29.r3479"), new Version("11.7.1.r3426")));

        /**
         * Version 11.9.30.3567
         */
        COMPATIBILITY_CHART.put(new Version("11.9.30.r3567"), Arrays.asList(
            new Version("11.9.30.r3567"), new Version("11.7.29.r3479"),
            new Version("11.7.1.r3426")));

        /**
         * Version 11.7.29.3479
         */
        COMPATIBILITY_CHART.put(new Version("11.7.29.r3479"), Arrays.asList(
            new Version("11.7.29.r3479"), new Version("11.7.1.r3426")));

        /**
         * Version 11.7.1.3426
         */
        COMPATIBILITY_CHART.put(new Version("11.7.1.r3426"),
            Arrays.asList(new Version("11.7.1.r3426")));

        /**
         * Version 11.5.6.r3294
         */
        COMPATIBILITY_CHART.put(new Version("11.5.6.r3294"), Arrays.asList(
            new Version("11.5.6.r3294"), new Version("11.3.25.r3201")));

        /**
         * Version 11.3.25.r3201
         */
        COMPATIBILITY_CHART.put(new Version("11.3.25.r3201"),
            Arrays.asList(new Version("11.3.25.r3201")));

        /**
         * Version 11.2.25.r3105
         */
        COMPATIBILITY_CHART.put(new Version("11.2.25.r3105"),
            Arrays.asList(new Version("11.2.25.r3105")));

        /**
         * Version 11.1.28.r2959
         */
        COMPATIBILITY_CHART.put(new Version("11.1.28.r2959"),
            Arrays.asList(new Version("11.1.28.r2959")));

        /**
         * Version 11.1.7.r2897
         */
        COMPATIBILITY_CHART.put(new Version("11.1.7.r2897"), Arrays.asList(
            new Version("11.1.7.r2897"), new Version("10.11.26.r2744"),
            new Version("10.10.29.r2640"), new Version("10.10.01.r2552"),
            new Version("10.8.27.r2333"), new Version("10.7.30.r2310")));

        /**
         * Version 10.11.26.r2744
         */
        COMPATIBILITY_CHART.put(new Version("10.11.26.r2744"), Arrays.asList(
            new Version("10.11.26.r2744"), new Version("10.10.29.r2640"),
            new Version("10.10.01.r2552"), new Version("10.8.27.r2333"),
            new Version("10.7.30.r2310")));

        /**
         * Version 10.10.29.r2640
         */
        COMPATIBILITY_CHART.put(new Version("10.10.29.r2640"), Arrays.asList(
            new Version("10.10.29.r2640"), new Version("10.10.01.r2552"),
            new Version("10.8.27.r2333"), new Version("10.7.30.r2310")));

        /**
         * Version 10.10.01.r2552
         */
        COMPATIBILITY_CHART.put(new Version("10.10.01.r2552"), Arrays.asList(
            new Version("10.10.01.r2552"), new Version("10.8.27.r2333"),
            new Version("10.7.30.r2310")));

        /**
         * Version 10.8.27.r2333
         */
        COMPATIBILITY_CHART.put(new Version("10.8.27.r2333"), Arrays.asList(
            new Version("10.8.27.r2333"), new Version("10.7.30.r2310")));

        /**
         * Version 10.7.30.r2310
         * 
         * CommunicationPreferences are now sent and received as part of the
         * InvitationInfo.
         */
        COMPATIBILITY_CHART.put(new Version("10.7.30.r2310"),
            Arrays.asList(new Version("10.7.30.r2310")));

        /**
         * Version 10.6.25.r2236
         * 
         * We are no longer backwards-compatible because of the changes in the
         * net refactoring. 10.6.25 isn't compatible to 10.6.11.r2223
         */
        COMPATIBILITY_CHART.put(new Version("10.6.25.r2236"),
            Arrays.asList(new Version("10.6.25.r2236")));

        /**
         * Version 10.6.11.r2223
         * 
         * We are no longer backwards-compatible because of the changes in the
         * net refactoring.
         */
        COMPATIBILITY_CHART.put(new Version("10.6.11.r2223"),
            Arrays.asList(new Version("10.6.11.r2223")));

        /**
         * Version 10.5.28.r2173
         */
        COMPATIBILITY_CHART.put(new Version("10.5.28.r2173"), Arrays.asList(
            new Version("10.5.28.r2173"), new Version("10.4.14.r2128")));

        /**
         * Version 10.4.14.r2128
         */
        COMPATIBILITY_CHART.put(new Version("10.4.14.r2128"),
            Arrays.asList(new Version("10.4.14.r2128")));

        /**
         * Version 10.3.26.r2105
         */
        COMPATIBILITY_CHART.put(new Version("10.3.26.r2105"),
            Arrays.asList(new Version("10.3.26.r2105")));
        /**
         * Version 10.2.26.r2037
         */
        COMPATIBILITY_CHART.put(new Version("10.2.26.r2037"),
            Arrays.asList(new Version("10.2.26.r2037")));

        /**
         * Version 10.1.29.r1970
         */
        COMPATIBILITY_CHART.put(new Version("10.1.29.r1970"),
            Arrays.asList(new Version("10.1.29.r1970")));

        /**
         * Version 9.12.04.r1862
         */
        COMPATIBILITY_CHART.put(new Version("9.12.4.r1878"),
            Arrays.asList(new Version("9.12.4.r1878")));

        /**
         * Version 9.10.30.r1833
         */
        COMPATIBILITY_CHART.put(new Version("9.10.30.r1833"),
            Arrays.asList(new Version("9.10.30.r1833")));

        /**
         * Version 9.10.30.DEVEL
         */
        COMPATIBILITY_CHART.put(new Version("9.10.30.DEVEL"),
            Arrays.asList(new Version("9.10.30.DEVEL")));

        /**
         * Version 9.10.2.r1803
         * 
         * We are not backward compatible because of changes in the invitation
         * process.
         */
        COMPATIBILITY_CHART.put(new Version("9.10.2.r1803"),
            Arrays.asList(new Version("9.10.2.r1803")));

        /**
         * Version 9.10.2.DEVEL
         */
        COMPATIBILITY_CHART.put(new Version("9.10.2.DEVEL"), Arrays.asList(
            new Version("9.10.2.DEVEL"), new Version("9.9.11.r1706"),
            new Version("9.9.11.DEVEL")));

        /**
         * Version 9.9.11.r1706
         */
        COMPATIBILITY_CHART.put(new Version("9.9.11.r1706"), Arrays.asList(
            new Version("9.9.11.r1706"), new Version("9.9.11.DEVEL")));

        /**
         * Version 9.9.11.DEVEL
         * 
         * No longer compatible with 9.8.21 since r.1665 changed compression of
         * Activities
         */
        COMPATIBILITY_CHART.put(new Version("9.9.11.DEVEL"),
            Arrays.asList(new Version("9.9.11.DEVEL")));

        /**
         * Version 9.8.21.r1660
         */
        COMPATIBILITY_CHART.put(new Version("9.8.21.r1660"), Arrays.asList(
            new Version("9.8.21.r1660"), new Version("9.8.21.DEVEL")));

        /**
         * Version 9.8.21.DEVEL
         * 
         * No longer compatible with 9.7.31 since r.1576 changed serialization
         * of Activities
         */
        COMPATIBILITY_CHART.put(new Version("9.8.21.DEVEL"),
            Arrays.asList(new Version("9.8.21.DEVEL")));
    }

    private final Version version;
    private final ITransmitter transmitter;
    private final IReceiver receiver;

    public VersionManager(@SarosVersion Version version,
        final IReceiver receiver, final ITransmitter transmitter) {

        this.version = version;
        this.receiver = receiver;
        this.transmitter = transmitter;

        receiver.addPacketListener(new PacketListener() {
            @Override
            public void processPacket(Packet packet) {

                @SuppressWarnings("unchecked")
                XStreamIQPacket<VersionInfo> iq = (XStreamIQPacket<VersionInfo>) packet;
                log.debug("Version request from " + iq.getFrom());

                VersionInfo remote = iq.getPayload();

                VersionInfo local = new VersionInfo();
                local.version = getVersion().toString();

                local.compatibility = determineCompatibility(
                    parseVersion(local.version), parseVersion(remote.version));

                IQ reply = VERSION_PROVIDER.createIQ(local);
                reply.setType(IQ.Type.RESULT);
                reply.setTo(iq.getFrom());

                try {
                    transmitter.sendPacket(reply, false);
                } catch (IOException e) {
                    log.error("could not send version info to " + iq.getFrom(),
                        e);
                }

                log.debug("send version info to " + iq.getFrom());
            }
        }, new AndFilter(VERSION_PROVIDER.getIQFilter(), new PacketFilter() {
            @Override
            public boolean accept(Packet packet) {
                return ((IQ) packet).getType() == IQ.Type.GET;

            }
        }));
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
     * @blocking If the request times out (10 seconds) or an error occurs null
     *           is returned.
     */
    public VersionInfo queryVersion(final JID rqJID) {

        assert rqJID.isResourceQualifiedJID();

        VersionInfo versionInfo = new VersionInfo();
        versionInfo.version = getVersion().toString();

        IQ request = VERSION_PROVIDER.createIQ(versionInfo);

        request.setType(IQ.Type.GET);
        request.setTo(rqJID.toString());

        SarosPacketCollector collector = receiver
            .createCollector(new AndFilter(VERSION_PROVIDER.getIQFilter(),
                new PacketFilter() {
                    @Override
                    public boolean accept(Packet packet) {
                        return rqJID.toString().equals(packet.getFrom())
                            && ((IQ) packet).getType() == IQ.Type.RESULT;
                    }
                }));

        try {
            transmitter.sendPacket(request, false);
            return VERSION_PROVIDER.getPayload(collector.nextResult(10 * 1000));
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
            return null;
        } finally {
            collector.cancel();
        }
    }

    /**
     * Returns the Version of the locally running Saros plugin
     */
    public Version getVersion() {
        return version;
    }

    /**
     * Will compare the two given Versions for compatibility. The result
     * indicates whether the localVersion passed first is compatible with the
     * remoteVersion.
     */
    public Compatibility determineCompatibility(Version localVersion,
        Version remoteVersion) {

        Compatibility compatibility = Compatibility.valueOf(compare(
            localVersion, remoteVersion));

        // remote version is lower than our version
        if (compatibility == Compatibility.TOO_NEW) {
            List<Version> compatibleVersions = COMPATIBILITY_CHART
                .get(localVersion);
            if (compatibleVersions != null
                && compatibleVersions.contains(remoteVersion)) {
                compatibility = Compatibility.OK;
            } else {
                log.error("VersionManager does not know about current version."
                    + " The release manager must have slept: " + localVersion);
            }
        }

        return compatibility;
    }

    /**
     * If the remote version is newer than the local one, the remote
     * compatibility comparison result will be returned.
     * 
     * @return A {@link VersionInfo} object with the remote
     *         {@link VersionInfo#version} and the ultimate
     *         {@link VersionInfo#compatibility} (based on both the local and
     *         the remote compatibility information) or <code>null</code> if
     *         could not get version information from the peer (this probably
     *         means the other person is TOO_OLD)
     * 
     *         The return value describes whether the local version is
     *         compatible with the peer's one (e.g.
     *         {@link Compatibility#TOO_OLD} means that the local version is too
     *         old)
     * 
     * @blocking This method may take some time (up to 7,5s) if the peer is not
     *           responding.
     */
    public VersionInfo determineCompatibility(JID peer) {
        return determineCompatibility(queryVersion(peer));
    }

    public VersionInfo determineCompatibility(VersionInfo remoteVersionInfo) {
        /*
         * FIXME Our caller should be able to distinguish whether the query
         * failed or it is an IM client which sends back the message
         */

        if (remoteVersionInfo == null)
            return null; // No answer from peer

        if (remoteVersionInfo.compatibility == null)
            return null; /*
                          * Peer does not understand our query and just sends it
                          * back to us. IMs like Pidgin do this.
                          */

        VersionInfo result = new VersionInfo();
        result.version = remoteVersionInfo.version;

        Compatibility localComp = determineCompatibility(getVersion(),
            parseVersion(remoteVersionInfo.version));

        Compatibility remoteComp = remoteVersionInfo.compatibility;

        if (localComp == Compatibility.TOO_OLD) {
            // Our version is older than the peer's one, let's trust his info
            if (remoteComp == Compatibility.OK) {
                // only if he tell's us that it is okay, return OK
                result.compatibility = Compatibility.OK;
            } else {
                // otherwise we are too old
                result.compatibility = Compatibility.TOO_OLD;
            }
        } else {
            // We are newer, thus we can just use our compatibility result
            result.compatibility = localComp;
        }
        return result;
    }

    /**
     * Given the bundle version string of a remote Saros instance, will return
     * whether the local version is compatible with the given instance.
     * 
     */
    public Compatibility determineCompatibility(String remoteVersionString) {

        Version remoteVersion = parseVersion(remoteVersionString);
        Version localVersion = getVersion();

        return determineCompatibility(localVersion, remoteVersion);
    }

    private Version parseVersion(String versionString) {
        Version version = Version.emptyVersion;
        try {
            version = Version.parseVersion(versionString);
        } catch (IllegalArgumentException e) {
            log.warn("version string of remote peer is illegally formatted: "
                + versionString, e);
        }
        return version;
    }

    /*
     * implements
     * http://www.osgi.org/javadoc/r4v43/org/osgi/framework/Version.html
     * #compareTo%28org.osgi.framework.Version%29 because interface signature
     * changed between Eclipse 3.6 and 3.7
     */
    private int compare(Version a, Version b) {
        int compareTo;

        compareTo = Integer.valueOf(a.getMajor()).compareTo(
            Integer.valueOf(b.getMajor()));

        if (compareTo != 0)
            return compareTo;

        compareTo = Integer.valueOf(a.getMinor()).compareTo(
            Integer.valueOf(b.getMinor()));

        if (compareTo != 0)
            return compareTo;

        compareTo = Integer.valueOf(a.getMicro()).compareTo(
            Integer.valueOf(b.getMicro()));

        if (compareTo != 0)
            return compareTo;

        return a.getQualifier().compareTo(b.getQualifier());
    }
}
