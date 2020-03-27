package saros.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.preference.IPreferenceStore;
import saros.net.xmpp.contact.XMPPContact;
import saros.preferences.PreferenceConstants;

/**
 * A manager class that allows to discover if a given XMPP entity supports Skype and that allows to
 * initiate Skype VOIP sessions with that entity.
 */
public class SkypeManager {

  private static final Logger log = Logger.getLogger(SkypeManager.class);

  private final InfoManager infoManager;
  private final IPreferenceStore preferenceStore;

  public SkypeManager(InfoManager infoManager, IPreferenceStore preferenceStore) {
    this.infoManager = infoManager;
    this.preferenceStore = preferenceStore;

    String localSkypeName = getLocalSkypeName();
    if (localSkypeName != null)
      infoManager.setLocalInfo(PreferenceConstants.SKYPE_USERNAME, localSkypeName);

    /** Register for our preference store, so we can be notified if the Skype user name changes. */
    preferenceStore.addPropertyChangeListener(
        event -> {
          if (event.getProperty().equals(PreferenceConstants.SKYPE_USERNAME)) {
            infoManager.setLocalInfo(PreferenceConstants.SKYPE_USERNAME, getLocalSkypeName());
          }
        });
  }

  /**
   * Returns the Skype name for user identified by the given XMPPContact.
   *
   * @return the skype name for given {@link XMPPContact} or <code>null</code> if the user has no
   *     skype name or it is not known yet
   */
  public String getSkypeName(XMPPContact contact) {
    return infoManager.getRemoteInfo(contact, PreferenceConstants.SKYPE_USERNAME).orElse(null);
  }

  /** @return the local Skype name or <code>null</code> if none is set. */
  private String getLocalSkypeName() {
    final String localSkypeName = preferenceStore.getString(PreferenceConstants.SKYPE_USERNAME);

    if (localSkypeName.trim().isEmpty()) return null;

    return localSkypeName;
  }

  // https://docs.microsoft.com/en-us/skype-sdk/skypeuris/skypeuriapireference

  public static String getChatCallUri(final String skypeName) {
    final String encoded = urlEncode(skypeName);
    return encoded == null ? null : "skype:" + encoded + "?chat";
  }

  public static String getAudioCallUri(final String skypeName) {
    final String encoded = urlEncode(skypeName);
    return encoded == null ? null : "skype:" + encoded;
  }

  public static String getVideoCallUri(final String skypeName) {
    final String encoded = urlEncode(skypeName);
    return encoded == null ? null : "skype:" + encoded + "?call&video=true";
  }

  public static boolean isEchoService(final String skypeName) {
    return "echo123".equalsIgnoreCase(skypeName);
  }

  private static String urlEncode(final String value) {

    String result = null;

    try {
      result = URLEncoder.encode(value, "UTF-8");
    } catch (UnsupportedEncodingException e) {

      log.warn("failed to url encode data:" + value, e);
    }

    return result;
  }

  private static Boolean isAvailable = null;

  /**
   * Crude check to see if Skype is available on the current system. This method may return <code>
   * true</code> even if <b>NO</b> Skype installation can be found. Subsequent calls will always
   * return the same result unless a refresh is performed.
   *
   * @param refresh if <code>true</code> a Skype installation will be searched again
   * @return <code>true</code> if Skype may be installed, <code>false</code> if it is definitely not
   *     installed
   */
  public static synchronized boolean isSkypeAvailable(final boolean refresh) {

    if (!refresh && isAvailable != null) return isAvailable;

    isAvailable = true;

    if (!SystemUtils.IS_OS_WINDOWS) return isAvailable;

    final ProcessBuilder builder = new ProcessBuilder("powershell");

    builder.command().add("-NonInteractive");
    builder.command().add("-command");
    builder
        .command()
        .add("$result=Get-AppxPackage -Name Microsoft.SkypeApp; if(!$result) { exit 1 }");

    builder.redirectErrorStream(true);

    InputStream in = null;

    final Process p;

    try {
      p = builder.start();

      in = p.getInputStream();

      while (in.read() != -1) {
        // NOP
      }

      isAvailable = p.waitFor() == 0;

    } catch (IOException | InterruptedException e) {
      log.warn("failed to determine Skype installation", e);
      isAvailable = true;

      if (e instanceof InterruptedException) Thread.currentThread().interrupt();
    } finally {
      IOUtils.closeQuietly(in);
    }

    return isAvailable;
  }
}
