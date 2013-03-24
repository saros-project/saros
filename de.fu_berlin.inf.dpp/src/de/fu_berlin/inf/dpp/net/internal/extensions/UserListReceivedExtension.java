package de.fu_berlin.inf.dpp.net.internal.extensions;

import de.fu_berlin.inf.dpp.annotations.Component;

@Component(module = "net")
public class UserListReceivedExtension extends SarosSessionPacketExtension {

    public static final Provider PROVIDER = new Provider();

    public UserListReceivedExtension(String sessionID) {
        super(sessionID);
    }

    public static class Provider extends
        SarosSessionPacketExtension.Provider<UserListReceivedExtension> {
        private Provider() {
            super("userListReceived", UserListReceivedExtension.class);
        }
    }
}
