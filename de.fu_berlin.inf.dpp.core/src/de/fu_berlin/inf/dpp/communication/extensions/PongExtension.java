package de.fu_berlin.inf.dpp.communication.extensions;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("PONG")
public class PongExtension extends SarosSessionPacketExtension {

  public static final Provider PROVIDER = new Provider();

  public PongExtension(String sessionID) {
    super(sessionID);
  }

  public static class Provider extends SarosSessionPacketExtension.Provider<PongExtension> {

    private Provider() {
      super("pong", PongExtension.class);
    }
  }
}
