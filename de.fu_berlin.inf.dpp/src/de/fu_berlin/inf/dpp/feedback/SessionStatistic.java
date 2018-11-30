package de.fu_berlin.inf.dpp.feedback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
import org.apache.commons.io.IOUtils;

/**
 * The SessionStatistic class wraps a <code>Properties</code> object in which the gathered
 * statistical data can be stored as simple key/value pairs. This data can then be saved to disk as
 * a file.
 *
 * <p>This class offers various <code>put</code> methods which optionally take suffixes to add to
 * the end of the key.
 *
 * <p>For example: <code><pre>
 *      SessionStatistic statistic = new SessionStatistic();
 *
 *      for (int i = 1; i < x; i++)
 *          put("foo.bar", true, i, "enabled");
 * </pre></code> Will add the boolean value <code>true</code> to the keys: <code>foo.bar.1.enabled
 * </code>, <code>foo.bar.2.enabled</code>, and so on ...
 *
 * <p>
 */
/*
 * TODO Add a field user.saros.team to the SessionStatistic which is set to true
 * if:
 *
 * the associated button on the FeedbackPreferencePage is true
 *
 * the Saros version number does not end with rXXXX
 *
 * the version number is just [X]X.[X]X.[X]X (with [X] being optional)
 *
 * The value should be determined by the SessionDataCollector and written to the
 * statistic on the end of a session.
 */
public class SessionStatistic {

  private static final String KEY_SESSION_ID = "session.id";

  /**
   * This is the {@link Properties} object to hold the statistical data. Properties are supposed to
   * store only strings for both keys and values, otherwise it can't be written to disk using
   * Properties.store().
   */
  private Properties data;

  public SessionStatistic() {
    data = new Properties();
  }

  /**
   * Adds the given boolean value to this statistic data using the provided key. The key can be
   * extended by specifying additional suffixes. It is recommended to use only integer or string
   * values to extend the key.
   *
   * @param key key the value is associated with, not <code>null</code>
   * @param value value to store
   * @param keySuffixes additional suffixes to extend the original key with
   */
  public void put(final String key, final boolean value, final Object... keySuffixes) {
    data.put(appendToKey(key, keySuffixes), String.valueOf(value));
  }

  /**
   * Adds the given integer value to this statistic data using the provided key. The key can be
   * extended by specifying additional suffixes. It is recommended to use only integer or string
   * values to extend the key.
   *
   * @param key key the value is associated with, not <code>null</code>
   * @param value value to store
   * @param keySuffixes additional suffixes to extend the original key with
   */
  public void put(final String key, final int value, final Object... keySuffixes) {
    data.put(appendToKey(key, keySuffixes), String.valueOf(value));
  }

  /**
   * Adds the given long value to this statistic data using the provided key. The key can be
   * extended by specifying additional suffixes. It is recommended to use only integer or string
   * values to extend the key.
   *
   * @param key key the value is associated with, not <code>null</code>
   * @param value value to store
   * @param keySuffixes additional suffixes to extend the original key with
   */
  public void put(final String key, final long value, final Object... keySuffixes) {
    data.put(appendToKey(key, keySuffixes), String.valueOf(value));
  }

  /**
   * Adds the given float value to this statistic data using the provided key. The key can be
   * extended by specifying additional suffixes. It is recommended to use only integer or string
   * values to extend the key.
   *
   * @param key key the value is associated with, not <code>null</code>
   * @param value value to store
   * @param keySuffixes additional suffixes to extend the original key with
   */
  public void put(final String key, final float value, final Object... keySuffixes) {
    data.put(appendToKey(key, keySuffixes), String.valueOf(value));
  }

  /**
   * Adds the given double value to this statistic data using the provided key. The key can be
   * extended by specifying additional suffixes. It is recommended to use only integer or string
   * values to extend the key.
   *
   * @param key key the value is associated with, not <code>null</code>
   * @param value value to store
   * @param keySuffixes additional suffixes to extend the original key with
   */
  public void put(final String key, final double value, final Object... keySuffixes) {
    data.put(appendToKey(key, keySuffixes), String.valueOf(value));
  }

  /**
   * Adds the given string value to this statistic data using the provided key. The key can be
   * extended by specifying additional suffixes. It is recommended to use only integer or string
   * values to extend the key.
   *
   * @param key key the value is associated with, not <code>null</code>
   * @param value value to store
   * @param keySuffixes additional suffixes to extend the original key with
   */
  public void put(final String key, final String value, final Object... keySuffixes) {
    data.put(appendToKey(key, keySuffixes), value);
  }

  /**
   * Adds the given date value to this statistic data using the provided key. The date will be
   * stored in UTC time using ISO8601. The key can be extended by specifying additional suffixes. It
   * is recommended to use only integer or string values to extend the key.
   *
   * @param key key the value is associated with, not <code>null</code>
   * @param value value to store, not <code>null</code>
   * @param keySuffixes additional suffixes to extend the original key with
   */
  public void put(final String key, final Date value, final Object... keySuffixes) {
    data.put(appendToKey(key, keySuffixes), toISO8601UTCTimeFormat(value));
  }

  /**
   * Adds the contents of the given SessionStatistic to this SessionStatistic.
   *
   * @param statistic
   */
  public void addAll(SessionStatistic statistic) {
    data.putAll(statistic.data);
  }

  @Override
  public String toString() {
    StringWriter out = new StringWriter(512);

    try {
      data.store(out, "Saros session data");
    } catch (IOException e) {
      // cannot happen
    } finally {
      IOUtils.closeQuietly(out);
    }
    return out.toString();
  }

  /**
   * Writes the session data to a file.
   *
   * @param file the file to save the current session statistic into
   */
  public void toFile(File file) throws IOException {

    FileOutputStream fos = null;

    // write the statistic to the file
    try {
      fos = new FileOutputStream(file);
      data.store(fos, "Saros session data");
    } finally {
      IOUtils.closeQuietly(fos);
    }
  }

  public void setSessionID(String sessionID) {
    data.setProperty(KEY_SESSION_ID, sessionID);
  }

  public String getSessionID() {
    return data.getProperty(KEY_SESSION_ID);
  }

  private String appendToKey(String key, Object... suffixes) {
    StringBuilder sb = new StringBuilder();
    sb.append(key);
    for (Object suffix : suffixes) {
      sb.append(".").append(suffix);
    }
    return sb.toString();
  }

  // need to be Java 6 compatible !
  private static String toISO8601UTCTimeFormat(Date date) {
    TimeZone timeZone = TimeZone.getTimeZone("UTC");
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    dateFormat.setTimeZone(timeZone);
    return dateFormat.format(date);
  }
}
