package saros.communication.extensions;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias(/* SessionNegotiationCompleted */ "SNCMP")
public class InvitationCompletedExtension extends InvitationExtension {
  public static final Provider PROVIDER = new Provider();

  public InvitationCompletedExtension(String invitationID) {
    super(invitationID);
  }

  public static class Provider extends InvitationExtension.Provider<InvitationCompletedExtension> {
    private Provider() {
      super("sncmp", InvitationAcknowledgedExtension.class);
    }
  }
}
