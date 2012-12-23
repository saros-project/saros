package de.fu_berlin.inf.dpp.net.internal.extensions;

public class UserListRequestExtension extends SarosSessionPacketExtension {

    public UserListRequestExtension(String sessionID) {
        super(sessionID);
    }

    public static class Provider extends
        XStreamExtensionProvider<UserListRequestExtension> {
        public Provider() {
            super("userListRequest", UserListRequestExtension.class);
        }
    }
}
