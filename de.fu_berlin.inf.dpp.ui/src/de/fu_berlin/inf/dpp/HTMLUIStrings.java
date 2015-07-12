package de.fu_berlin.inf.dpp;

/**
 * <p>
 * This class encapsulates all labels, messages, titles, etc. which will be
 * visible in the HTML UI. <br>
 * TODO: Maybe this feature can be implemented more sophisticated in the future.
 */
public class HTMLUIStrings {

    private HTMLUIStrings() {
        // Hide implicit public constructor
    }

    // dialog titles
    public static final String MAIN_PAGE_TITLE = "Main page";
    public static final String START_SESSION_WIZARD_TITLE = "Share Project";
    public static final String ADD_ACCOUNT_PAGE_TITLE = "Add Account";

    // error messages
    public static final String START_SESSION_CANCELD = "Couldn't send session invitaion.";
    public static final String INVALID_JID = "The Jabber ID must be in the format user@domain.";
}
