package de.fu_berlin.inf.dpp.communication.extensions;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("JSRq")
public class JoinSessionRequestExtension extends SarosPacketExtension {

  public static final Provider PROVIDER = new Provider();

  public static class Provider extends SarosPacketExtension.Provider<JoinSessionRequestExtension> {

    private Provider() {
      super("joinSessionRequest", JoinSessionRequestExtension.class);
    }
  }
}
