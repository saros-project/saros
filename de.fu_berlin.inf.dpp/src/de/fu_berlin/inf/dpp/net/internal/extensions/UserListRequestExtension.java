package de.fu_berlin.inf.dpp.net.internal.extensions;

public class UserListRequestExtension extends SarosSessionPacketExtension {

    public static final Provider PROVIDER = new Provider();

    public UserListRequestExtension(String sessionID) {
        super(sessionID);
    }

    public static class Provider extends
        SarosSessionPacketExtension.Provider<UserListRequestExtension> {
        private Provider() {
            super("userListRequest", UserListRequestExtension.class);
        }
    }
}
