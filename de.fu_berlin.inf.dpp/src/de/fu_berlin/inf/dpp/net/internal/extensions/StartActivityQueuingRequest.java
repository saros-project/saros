package de.fu_berlin.inf.dpp.net.internal.extensions;


public class StartActivityQueuingRequest extends ProjectNegotiationExtension {

    public static final Provider PROVIDER = new Provider();

    public StartActivityQueuingRequest(String sessionID, String negotiationID) {
        super(sessionID, negotiationID);
    }

    public static class Provider extends
        ProjectNegotiationExtension.Provider<StartActivityQueuingRequest> {

        private Provider() {
            super("startActivityQueuingRequest",
                StartActivityQueuingRequest.class);
        }
    }
}
