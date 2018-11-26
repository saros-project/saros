package de.fu_berlin.inf.dpp.communication.extensions;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias(/* StartActivityQueuingRequest */ "SAQRQ")
public class StartActivityQueuingRequest extends ProjectNegotiationExtension {

  public static final Provider PROVIDER = new Provider();

  public StartActivityQueuingRequest(String sessionID, String negotiationID) {
    super(sessionID, negotiationID);
  }

  public static class Provider
      extends ProjectNegotiationExtension.Provider<StartActivityQueuingRequest> {

    private Provider() {
      super("saqrq", StartActivityQueuingRequest.class);
    }
  }
}
