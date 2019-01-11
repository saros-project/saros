package de.fu_berlin.inf.dpp.communication.extensions;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("SSRq")
public class SessionStatusRequestExtension extends SarosPacketExtension {

  public static final Provider PROVIDER = new Provider();

  public static class Provider
      extends SarosPacketExtension.Provider<SessionStatusRequestExtension> {
    private Provider() {
      super("sessionStatusRequest", SessionStatusRequestExtension.class);
    }
  }
}
