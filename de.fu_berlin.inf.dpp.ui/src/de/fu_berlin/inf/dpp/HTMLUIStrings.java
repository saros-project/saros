package de.fu_berlin.inf.dpp;

/**
 * This class encapsulates all labels, messages, titles, etc. which will be
 * visible in the HTML UI. <br>
 * 
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
    // TODO: Convention for naming error messages like ERR_XYZ!?
    // session
    public static final String START_SESSION_CANCELED = "Couldn't send session invitaion.";
    public static final String PROJECT_LIST_IOEXCEPTION = "An error occurred while trying to create a list of all files to share.";
    // contact
    public static final String INVALID_JID = "The Jabber ID must be in the format user@domain.";
    public static final String DELETE_CONTACT_FAILED = "Failed to delete contact.";
    public static final String ADD_CONTACT_FAILED = "Failed to add contact.";
    public static final String RENAME_CONTACT_FAILED = "Failed to rename contact.";
    // account
    public static final String SAVE_ACCOUNT_ALREADY_PRESENT = "Couldn't create account. Account already present";
    public static final String EDIT_ACCOUNT_FAILED = "Couldn't change the account.";
    public static final String SET_ACTIVE_ACCOUNT_FAILED = "Error while trying to change the active account.";
    public static final String ERR_DELETE_ACTIVE_ACCOUNT = "Couldn't delete the account. The currently active account cannot be deleted";
}
