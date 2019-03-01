package saros;

/**
 * This class encapsulates all labels, messages, titles, etc. which will be visible in the HTML UI.
 * <br>
 * TODO: Maybe this feature can be implemented more sophisticated in the future.
 */
public class HTMLUIStrings {

  private HTMLUIStrings() {
    // Hide implicit public constructor
  }

  // dialog titles
  public static final String TITLE_MAIN_PAGE = "Main page";
  public static final String TITLE_START_SESSION_WIZARD = "Share Project";
  public static final String TITLE_ADD_ACCOUNT_PAGE = "Add Account";
  public static final String TITLE_CONFIGURATION_PAGE = "Configure Saros";

  // error messages
  public static final String ERR_ACCOUNT_ALREADY_PRESENT =
      "Couldn't create account. Account already present";
  public static final String ERR_ACCOUNT_EDIT_FAILED = "Couldn't change the account.";
  public static final String ERR_ACCOUNT_SET_ACTIVE_FAILED =
      "Error while trying to change the active account.";
  public static final String ERR_ACCOUNT_DELETE_ACTIVE =
      "Couldn't delete the account. The currently active account cannot be deleted";

  public static final String ERR_CONTACT_INVALID_JID =
      "The Jabber ID must be in the format user@domain.";
  public static final String ERR_CONTACT_DELETE_FAILED = "Failed to delete contact.";
  public static final String ERR_CONTACT_ADD_FAILED = "Failed to add contact.";
  public static final String ERR_CONTACT_RENAME_FAILED = "Failed to rename contact.";

  public static final String ERR_SESSION_START_CANCELED = "Couldn't send session invitaion.";
  public static final String ERR_SESSION_PROJECT_LIST_IOEXCEPTION =
      "An error occurred while trying to create a list of all files to share.";
}
