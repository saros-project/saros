package saros.communication.extensions;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias(/* StartActivityQueuingRequest */ "SAQRQ")
public class StartActivityQueuingRequest extends ResourceNegotiationExtension {

  public static final Provider PROVIDER = new Provider();

  public StartActivityQueuingRequest(String sessionID, String negotiationID) {
    super(sessionID, negotiationID);
  }

  public static class Provider
      extends ResourceNegotiationExtension.Provider<StartActivityQueuingRequest> {

    private Provider() {
      super("saqrq", StartActivityQueuingRequest.class);
    }
  }
}
