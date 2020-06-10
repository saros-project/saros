package saros.communication.extensions;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import saros.misc.xstream.JIDConverter;
import saros.net.xmpp.JID;

@XStreamAlias(/* ResourceNegotiationCompleted */ "RNCMP")
public class UserFinishedResourceNegotiationExtension extends SarosSessionPacketExtension {

  public static final Provider PROVIDER = new Provider();

  @XStreamAsAttribute
  @XStreamConverter(JIDConverter.class)
  private JID jid;

  public UserFinishedResourceNegotiationExtension(String sessionID, JID jid) {
    super(sessionID);
    this.jid = jid;
  }

  public JID getJID() {
    return jid;
  }

  public static class Provider
      extends SarosSessionPacketExtension.Provider<UserFinishedResourceNegotiationExtension> {

    private Provider() {
      super("rncmp", UserFinishedResourceNegotiationExtension.class);
    }
  }
}
