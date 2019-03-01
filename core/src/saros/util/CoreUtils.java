package saros.util;

public class CoreUtils {

  /**
   * Returns a string representation of the throughput when processing the given number of bytes in
   * the given time in milliseconds.
   */
  public static String throughput(long length, long deltaMs) {

    String duration = null;

    if (deltaMs == 0) {
      duration = "< 1 ms";
      deltaMs = 1;
    }

    if (duration == null) duration = deltaMs < 1000 ? "< 1 s" : formatDuration(deltaMs / 1000);

    return formatByte(length)
        + " in "
        + duration
        + " at "
        + formatByte(length / deltaMs * 1000)
        + "/s";
  }

  /**
   * Turns a long representing a file size in bytes into a human readable representation based on
   * 1KB = 1000 Byte, 1000 KB=1MB, etc. (SI)
   */
  public static String formatByte(long bytes) {
    int unit = 1000;
    if (bytes < unit) return bytes + " B";
    int exp = (int) (Math.log(bytes) / Math.log(unit));
    char pre = ("kMGTPE").charAt(exp - 1);
    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
  }

  /**
   * Formats a given duration in seconds (e.g. achieved by using a StopWatch) as HH:MM:SS
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
    format += minutes > 0 ? String.format(hours > 0 ? "%02d" : "%d", minutes) + "m " : "";
    format += seconds > 0 ? String.format(minutes > 0 ? "%02d" : "%d", secs) + "s" : "";
    return format;
  }
}
