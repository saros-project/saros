package saros.communication.extensions;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias(/* StartActivityQueuingResponse */ "SAQRP")
public class StartActivityQueuingResponse extends ResourceNegotiationExtension {

  public static final Provider PROVIDER = new Provider();

  public StartActivityQueuingResponse(String sessionID, String negotiationID) {
    super(sessionID, negotiationID);
  }

  public static class Provider
      extends ResourceNegotiationExtension.Provider<StartActivityQueuingResponse> {

    private Provider() {
      super("saqrp", StartActivityQueuingResponse.class);
    }
  }
}
