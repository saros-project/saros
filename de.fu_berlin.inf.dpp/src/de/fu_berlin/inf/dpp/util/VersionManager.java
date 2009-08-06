package de.fu_berlin.inf.dpp.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

import de.fu_berlin.inf.dpp.annotations.Component;

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

    protected Bundle bundle;

    public VersionManager(Bundle bundle) {
        this.bundle = bundle;
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
