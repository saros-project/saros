/** */
package de.fu_berlin.inf.dpp.communication.extensions;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias(/* SessionNegotiationCancel */ "SNCL")
public class CancelInviteExtension extends InvitationExtension {

  public static final Provider PROVIDER = new Provider();

  @XStreamAlias("error")
  private String errorMessage;

  public CancelInviteExtension(String invitationID, String errorMessage) {
    super(invitationID);
    if ((errorMessage != null) && (errorMessage.length() > 0)) this.errorMessage = errorMessage;
  }

  /**
   * Returns the error message for this cancellation.
   *
   * @return the error message or <code>null</code> if the remote contact canceled the invitation
   *     manually
   */
  public String getErrorMessage() {
    return errorMessage;
  }

  public static class Provider extends InvitationExtension.Provider<CancelInviteExtension> {
    private Provider() {
      super("sncl", CancelInviteExtension.class);
    }
  }
}
