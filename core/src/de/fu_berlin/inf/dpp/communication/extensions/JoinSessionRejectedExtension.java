package de.fu_berlin.inf.dpp.communication.extensions;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("JSRj")
public class JoinSessionRejectedExtension extends SarosPacketExtension {

  public static final Provider PROVIDER = new Provider();

  public static class Provider extends SarosPacketExtension.Provider<JoinSessionRejectedExtension> {

    private Provider() {
      super("joinRequestRejected", JoinSessionRejectedExtension.class);
    }
  }
}
