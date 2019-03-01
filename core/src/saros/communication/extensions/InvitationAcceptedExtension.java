package saros.communication.extensions;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias(/* SessionNegotiationAccepted */ "SNAC")
public class InvitationAcceptedExtension extends InvitationExtension {

  public static final Provider PROVIDER = new Provider();

  public InvitationAcceptedExtension(String invitationID) {
    super(invitationID);
  }

  public static class Provider extends InvitationExtension.Provider<InvitationAcceptedExtension> {
    private Provider() {
      super("snac", InvitationAcceptedExtension.class);
    }
  }
}
