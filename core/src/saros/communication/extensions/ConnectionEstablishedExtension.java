package de.fu_berlin.inf.dpp.communication.extensions;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias(/* ConnectionEstablishedExtension */ "COES")
public class ConnectionEstablishedExtension extends InvitationExtension {

  public static final Provider PROVIDER = new Provider();

  public ConnectionEstablishedExtension(String invitationID) {
    super(invitationID);
  }

  public static class Provider
      extends InvitationExtension.Provider<ConnectionEstablishedExtension> {
    private Provider() {
      super("coes", ConnectionEstablishedExtension.class);
    }
  }
}
