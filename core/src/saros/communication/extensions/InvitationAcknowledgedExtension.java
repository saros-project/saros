package saros.communication.extensions;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias(/* SessionNegotiationAcknowledged */ "SNAK")
public class InvitationAcknowledgedExtension extends InvitationExtension {

  public static final Provider PROVIDER = new Provider();

  public InvitationAcknowledgedExtension(String invitationID) {
    super(invitationID);
  }

  public static class Provider
      extends InvitationExtension.Provider<InvitationAcknowledgedExtension> {
    private Provider() {
      super("snak", InvitationAcknowledgedExtension.class);
    }
  }
}
