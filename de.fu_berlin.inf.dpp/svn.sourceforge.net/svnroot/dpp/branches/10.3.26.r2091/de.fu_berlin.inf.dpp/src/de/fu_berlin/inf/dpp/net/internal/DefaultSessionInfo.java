package de.fu_berlin.inf.dpp.net.internal;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;

@Component(module = "net")
public class DefaultSessionInfo {
    public String sessionID;

    public DefaultSessionInfo(SessionIDObservable sessionID) {
        this.sessionID = sessionID.getValue();
    }

    /*
     * Simple provider classes which do not have any special information
     * content, but are based on the DefaultSessionInfo, e.g. simple requests
     */
    public static class UserListConfirmationExtensionProvider extends
        XStreamExtensionProvider<DefaultSessionInfo> {
        public UserListConfirmationExtensionProvider() {
            super("userListConfirmation", String.class);
        }
    }
}
