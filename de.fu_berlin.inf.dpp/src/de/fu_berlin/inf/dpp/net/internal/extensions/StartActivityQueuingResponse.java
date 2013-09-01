package de.fu_berlin.inf.dpp.net.internal.extensions;


public class StartActivityQueuingResponse extends ProjectNegotiationExtension {

    public static final Provider PROVIDER = new Provider();

    public StartActivityQueuingResponse(String sessionID, String negotiationID) {
        super(sessionID, negotiationID);
    }

    public static class Provider extends
        ProjectNegotiationExtension.Provider<StartActivityQueuingResponse> {

        private Provider() {
            super("startActivityQueuingRequest",
                StartActivityQueuingResponse.class);
        }
    }
}