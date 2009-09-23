package de.fu_berlin.inf.dpp.net.internal;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;

@Component(module = "net")
public class DefaultInvitationInfo {
    public String sessionID;
    public String invitationID;

    public DefaultInvitationInfo(SessionIDObservable sessionID,
        String invitationID) {
        this.sessionID = sessionID.getValue();
        this.invitationID = invitationID;
    }

    /*
     * Simple provider classes which do not have any special information
     * content, e.g. simple requests
     */

    public static class FileListRequestExtensionProvider extends
        XStreamExtensionProvider<DefaultInvitationInfo> {
        public FileListRequestExtensionProvider() {
            super("fileListRequest", DefaultInvitationInfo.class);
        }
    }

    public static class UserListConfirmationExtensionProvider extends
        XStreamExtensionProvider<String> {
        public UserListConfirmationExtensionProvider() {
            super("userListConfirmation", String.class);
        }
    }
}
