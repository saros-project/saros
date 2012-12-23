package de.fu_berlin.inf.dpp.net.internal.extensions;

import de.fu_berlin.inf.dpp.annotations.Component;

@Component(module = "net")
public class UserListReceivedExtension extends SarosSessionPacketExtension {

    public UserListReceivedExtension(String sessionID) {
        super(sessionID);
    }

    public static class Provider extends
        XStreamExtensionProvider<UserListReceivedExtension> {
        public Provider() {
            super("userListReceived", UserListReceivedExtension.class);
        }
    }
}
