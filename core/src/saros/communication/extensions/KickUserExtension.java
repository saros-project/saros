package saros.communication.extensions;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias(/* SessionKickUser */ "SNKU")
public class KickUserExtension extends SarosSessionPacketExtension {

  public static final Provider PROVIDER = new Provider();

  public KickUserExtension(String sessionID) {
    super(sessionID);
  }

  public static class Provider extends SarosSessionPacketExtension.Provider<KickUserExtension> {
    private Provider() {
      super("snku", KickUserExtension.class);
    }
  }
}
