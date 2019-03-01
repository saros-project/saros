package saros.communication.extensions;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import java.util.HashMap;
import java.util.Map;
import saros.misc.xstream.XStreamExtensionProvider;

/**
 * Packet containing data for exchanging version details. This packet extension is <b>NOT</b>
 * affected by the current Saros Extension protocol version because it must be always possible to
 * communicate with older and newer Saros Version to determine compatibility.
 *
 * <p>To offer the most generic solution possible this packet does not contain any detailed data but
 * allows exchanging variable data in it is string representation.
 */
@XStreamAlias(/* VersionExchangeExtension */ "VEREX")
public class VersionExchangeExtension {

  public static final Provider PROVIDER = new Provider();

  @XStreamAlias("data")
  private final Map<String, String> data = new HashMap<String, String>();

  /**
   * Associates the specified value with the specified key.
   *
   * @param key key with which the specified value is to be associated
   * @param value value to be associated with the specified key
   * @return the previous value associated with key, or <code>null</code> if there was no mapping
   *     for key
   */
  public String set(String key, String value) {
    return data.put(key, value);
  }

  /**
   * Returns the value to which the specified key is mapped, or null if this map contains no mapping
   * for the key.
   *
   * @param key the key whose associated value is to be returned
   * @return the value to which the specified key is mapped, or null if this map contains no mapping
   *     for the key
   */
  public String get(String key) {
    return data.get(key);
  }

  public static class Provider extends XStreamExtensionProvider<VersionExchangeExtension> {

    private Provider() {
      super(SarosPacketExtension.EXTENSION_NAMESPACE, "verex", VersionExchangeExtension.class);
    }
  }
}
