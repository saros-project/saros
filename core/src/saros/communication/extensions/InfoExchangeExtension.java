package saros.communication.extensions;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import saros.misc.xstream.XStreamExtensionProvider;

/**
 * Packet containing data for exchanging client infos. This packet extension is <b>NOT</b> affected
 * by the current Saros Extension protocol version because it should be always possible to
 * communicate with older and newer Saros Version to determine compatibility.
 *
 * <p>To offer the most generic solution possible this packet does not contain any detailed data but
 * allows exchanging variable data in it is string representation.
 */
@XStreamAlias(/* InfoExchangeExtension */ "INFO")
public class InfoExchangeExtension {

  public static final Provider PROVIDER = new Provider();

  @XStreamAlias("data")
  private final Map<String, String> data;

  public InfoExchangeExtension(Map<String, String> data) {
    if (data == null) this.data = Collections.emptyMap();
    else this.data = new HashMap<>(data);
  }

  /**
   * Returns the data map.
   *
   * @return data map
   */
  public Map<String, String> getData() {
    return Collections.unmodifiableMap(data);
  }

  public static class Provider extends XStreamExtensionProvider<InfoExchangeExtension> {
    private Provider() {
      super(SarosPacketExtension.EXTENSION_NAMESPACE, "info", InfoExchangeExtension.class);
    }
  }
}
