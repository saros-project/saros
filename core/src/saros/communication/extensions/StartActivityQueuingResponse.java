package saros.communication.extensions;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias(/* StartActivityQueuingResponse */ "SAQRP")
public class StartActivityQueuingResponse extends ProjectNegotiationExtension {

  public static final Provider PROVIDER = new Provider();

  public StartActivityQueuingResponse(String sessionID, String negotiationID) {
    super(sessionID, negotiationID);
  }

  public static class Provider
      extends ProjectNegotiationExtension.Provider<StartActivityQueuingResponse> {

    private Provider() {
      super("saqrp", StartActivityQueuingResponse.class);
    }
  }
}
