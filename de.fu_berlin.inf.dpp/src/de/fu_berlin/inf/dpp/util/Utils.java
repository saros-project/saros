package de.fu_berlin.inf.dpp.util;

import java.io.File;
import java.util.Iterator;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;

import de.fu_berlin.inf.dpp.net.JID;

/**
 * Static Utility functions
 */
public final class Utils {

    private static final Logger log = Logger.getLogger(Utils.class);

    private Utils() {
        // no instantiation allowed
    }

    /**
     * Returns an iterable which will return the given iterator ONCE.
     * 
     * Subsequent calls to iterator() will throw an IllegalStateException.
     * 
     * @param <T>
     * @param it
     *            an Iterator to wrap
     * @return an Iterable which returns the given iterator ONCE.
     */
    public static <T> Iterable<T> asIterable(final Iterator<T> it) {
        return new Iterable<T>() {

            boolean returned = false;

            @Override
            public Iterator<T> iterator() {
                if (returned)
                    throw new IllegalStateException(
                        "Can only call iterator() once.");

                returned = true;

                return it;
            }
        };
    }

    /**
     * Utility method similar to {@link ObjectUtils#equals(Object, Object)},
     * which causes a compile error if the second parameter is not a subclass of
     * the first.
     */
    public static <V, K extends V> boolean equals(V object1, K object2) {
        return ObjectUtils.equals(object1, object2);
    }

    public static String escapeForLogging(String s) {
        if (s == null)
            return null;

        return StringEscapeUtils.escapeJava(s);
        /*
         * // Try to put nice symbols for non-readable characters sometime
         * return s.replace(' ', '\uc2b7').replace('\t',
         * '\uc2bb').replace('\n','\uc2b6').replace('\r', '\uc2a4');
         */
    }

    private static String getEclipsePlatformInfo() {
        return Platform.getBundle("org.eclipse.core.runtime").getVersion()
            .toString();
    }

    public static String getPlatformInfo() {

        String javaVersion = System.getProperty("java.version",
            "Unknown Java Version");
        String javaVendor = System.getProperty("java.vendor", "Unknown Vendor");
        String os = System.getProperty("os.name", "Unknown OS");
        String osVersion = System.getProperty("os.version", "Unknown Version");
        String hardware = System.getProperty("os.arch", "Unknown Architecture");

        StringBuilder sb = new StringBuilder();

        sb.append("  Java Version: " + javaVersion + "\n");
        sb.append("  Java Vendor: " + javaVendor + "\n");
        sb.append("  Eclipse Runtime Version: " + getEclipsePlatformInfo()
            + "\n");
        sb.append("  Operating System: " + os + " (" + osVersion + ")\n");
        sb.append("  Hardware Architecture: " + hardware);

        return sb.toString();
    }

    // should be moved to JID class
    public static String prefix(JID jid) {
        if (jid == null) {
            return "[Unknown] ";
        } else {
            return "[" + jid.toString() + "] ";
        }
    }

    /**
     * Given a *File*name, this function will ensure that if the filename
     * contains directories, these directories are created.
     * 
     * Returns true if the directory of the given file already exists or the
     * file has no parent directory.
     * 
     * Returns false, if the directory could not be created.
     */
    public static boolean mkdirs(String filename) {
        File parent = new File(filename).getParentFile();

        if (parent == null || parent.isDirectory()) {
            return true;
        }

        if (!parent.mkdirs()) {
            log.error("Could not create Dir: " + parent.getPath());
            return false;
        } else
            return true;
    }

    /**
     * Returns a string representation of the throughput when processing the
     * given number of bytes in the given time in milliseconds.
     */
    public static String throughput(long length, long deltaMs) {

        String duration = null;

        if (deltaMs == 0) {
            duration = "< 1 ms";
            deltaMs = 1;
        }

        if (duration == null)
            duration = deltaMs < 1000 ? "< 1 s"
                : formatDuration(deltaMs / 1000);

        return formatByte(length) + " in " + duration + " at "
            + formatByte(length / deltaMs * 1000) + "/s";
    }

    /**
     * Turns a long representing a file size in bytes into a human readable
     * representation based on 1KB = 1000 Byte, 1000 KB=1MB, etc. (SI)
     */
    public static String formatByte(long bytes) {
        int unit = 1000;
        if (bytes < unit)
            return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        char pre = ("kMGTPE").charAt(exp - 1);
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    /**
     * Formats a given duration in seconds (e.g. achived by using a StopWatch)
     * as HH:MM:SS
     * 
     * @param seconds
     * @return
     */
    public static String formatDuration(long seconds) {
        String format = "";

        if (seconds <= 0L) {
            return "";
        }
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = (seconds % 60);

        format += hours > 0 ? String.format("%02d", hours) + "h " : "";
        format += minutes > 0 ? String.format(hours > 0 ? "%02d" : "%d",
            minutes) + "m " : "";
        format += seconds > 0 ? String
            .format(minutes > 0 ? "%02d" : "%d", secs) + "s" : "";
        return format;
    }
}
