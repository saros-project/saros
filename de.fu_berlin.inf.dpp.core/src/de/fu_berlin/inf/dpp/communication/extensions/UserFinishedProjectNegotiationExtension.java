package de.fu_berlin.inf.dpp.communication.extensions;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import de.fu_berlin.inf.dpp.misc.xstream.JIDConverter;
import de.fu_berlin.inf.dpp.net.xmpp.JID;

@XStreamAlias(/* ProjectNegotiationCompleted */ "PNCMP")
public class UserFinishedProjectNegotiationExtension extends SarosSessionPacketExtension {

  public static final Provider PROVIDER = new Provider();

  @XStreamAsAttribute
  @XStreamConverter(JIDConverter.class)
  private JID jid;

  public UserFinishedProjectNegotiationExtension(String sessionID, JID jid) {
    super(sessionID);
    this.jid = jid;
  }

  public JID getJID() {
    return jid;
  }

  public static class Provider
      extends SarosSessionPacketExtension.Provider<UserFinishedProjectNegotiationExtension> {

    private Provider() {
      super("pncmp", UserFinishedProjectNegotiationExtension.class);
    }
  }
}
