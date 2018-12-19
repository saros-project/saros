package de.fu_berlin.inf.dpp.stf.shared;

import static de.fu_berlin.inf.dpp.stf.shared.Configuration.get;

public interface Constants {
  public enum TypeOfCreateProject {
    NEW_PROJECT,
    EXIST_PROJECT,
    EXIST_PROJECT_WITH_COPY
  }

  public enum TypeOfShareProject {
    SHARE_PROJECT,
    SHARE_PROJECT_PARTICALLY,
    ADD_SESSION
  }

  public enum TreeItemType {
    JAVA_PROJECT,
    PROJECT,
    FILE,
    CLASS,
    PKG,
    FOLDER,
    NULL
  }

  public static final String CONFIRM_DELETE = "Confirm Delete";
  public static final String DELETE = "Delete";
  public static final String SHELL_COPY_PROJECT = "Copy Project";
  public static final String SHELL_DELETING_ACTIVE_ACCOUNT = "Deleting active account";

  /*
   * DECORATORS
   */

  public static final String PROJECT_SHARED_DECORATOR = get("SharedProjectDecorator_shared");

  public static final String PROJECT_PARTIAL_SHARED_DECORATOR =
      get("SharedProjectDecorator_shared_partial");

  /* *********************************************
   *
   * Basic Wigets
   *
   * ********************************************
   */
  // Title of Buttons
  public static final String YES = "Yes";
  public static final String OK = "OK";
  public static final String NO = "No";
  public static final String CANCEL = "Cancel";
  public static final String FINISH = "Finish";
  public static final String ACCEPT = "Accept";
  public static final String APPLY = "Apply";
  public static final String NEXT = "Next >";
  public static final String BACK = "< Back";
  public static final String BROWSE = "Browse";
  public static final String OVERWRITE = "Overwrite";
  public static final String BACKUP = "Create Backup";
  public static final String RUN_IN_BACKGROUND = "Run in Background";
  public static final String RESTORE_DEFAULTS = "Restore Defaults";

  public static final String SRC = "src";
  public static final String SUFFIX_JAVA = ".java";

  /* *********************************************
   *
   * View Progress
   *
   * ********************************************
   */
  public static final String SHELL_PROGRESS_INFORMATION = "Progress Information";

  /* *********************************************
   *
   * Dialog Preferences
   *
   * ********************************************
   */
  public static final String NODE_CONSOLE = "Console";
  public static final String NODE_EDITORS = "Editors";
  public static final String NODE_TEXT_EDITORS = "Text Editors";
  public static final String NODE_ANNOTATIONS = "Annotations";
  /* *********************************************
   *
   * Main Menu File
   *
   * ********************************************
   */

  public static final String MENU_NEW = "New";
  public static final String MENU_PROJECT = "Project...";
  public static final String MENU_FOLDER = "Folder";
  public static final String MENU_FILE = "File";
  public static final String MENU_CLASS = "Class";
  public static final String MENU_PACKAGE = "Package";
  public static final String MENU_JAVA_PROJECT = "Java Project";
  public static final String MENU_CLOSE = "Close";
  public static final String MENU_CLOSE_ALL = "Close All";
  public static final String MENU_REFRESH = "Refresh";
  public static final String MENU_SAVE = "Save";
  public static final String MENU_SAVE_AS = "Save As...";
  public static final String MENU_SAVE_All = "Save All";

  public static final String SHELL_NEW_FOLDER = "New Folder";
  public static final String SHELL_NEW_FILE = "New File";
  public static final String SHELL_NEW_JAVA_PACKAGE = "New Java Package";
  public static final String SHELL_NEW_JAVA_CLASS = "New Java Class";
  public static final String SHELL_NEW_PROJECT = "New Project";
  public static final String SHELL_NEW_JAVA_PROJECT = "New Java Project";

  /* categories and nodes of the shell "New Project" */
  public static final String NODE_GENERAL = "General";
  public static final String NODE_PROJECT = "Project";

  /* Eclipse Project Wizard */
  public static final String LABEL_PROJECT_NAME = "Project name:";
  public static final String LABEL_FILE_NAME = "File name:";
  public static final String LABEL_FOLDER_NAME = "Folder name:";
  public static final String LABEL_ENTER_OR_SELECT_THE_PARENT_FOLDER =
      "Enter or select the parent folder:";
  public static final String LABEL_SOURCE_FOLDER = "Source folder:";
  public static final String LABEL_PACKAGE = "Package:";
  public static final String LABEL_NAME = "Name:";

  /* *********************************************
   *
   * Main Menu Edit
   *
   * ********************************************
   */
  public static final String SHELL_DELETE_RESOURCE = "Delete Resources";

  /* menu names */
  public static final String MENU_DELETE = "Delete";
  public static final String MENU_EDIT = "Edit";
  public static final String MENU_COPY = "Copy";
  public static final String MENU_PASTE = "Paste";

  /* *********************************************
   *
   * Main Menu Refactor
   *
   * ********************************************
   */
  /* shell titles */
  public static final String SHELL_MOVE = "Move";
  public static final String SHELL_RENAME_PACKAGE = "Rename Package";
  public static final String SHELL_RENAME_JAVA_PROJECT = "Rename Java Project";
  public static final String SHELL_RENAME_RESOURCE = "Rename Resource";
  public static final String SHELL_RENAME_COMPIIATION_UNIT = "Rename Compilation Unit";
  public static final String LABEL_NEW_NAME = "New name:";

  /* menu names */
  public static final String MENU_REFACTOR = "Refactor";
  public static final String MENU_RENAME = "Rename...";
  public static final String MENU_MOVE = "Move...";

  /* *********************************************
   *
   * Main Menu Window
   *
   * ********************************************
   */

  public static final String TREE_ITEM_GENERAL_IN_PRFERENCES = "General";
  public static final String TREE_ITEM_WORKSPACE_IN_PREFERENCES = "Workspace";
  public static final String TREE_ITEM_GENERAL_IN_SHELL_SHOW_VIEW = "General";
  public static final String TREE_ITEM_PROBLEM_IN_SHELL_SHOW_VIEW = "Problems";
  public static final String TREE_ITEM_PROJECT_EXPLORER_IN_SHELL_SHOW_VIEW = "Project Explorer";

  /* name of all the main menus */
  public static final String MENU_WINDOW = "Window";
  public static final String MENU_OTHER = "Other...";
  public static final String MENU_SHOW_VIEW = "Show View";

  public static final String SHELL_SHOW_VIEW = "Show View";

  /* IDs of all the perspectives */
  public static final String ID_JAVA_PERSPECTIVE = "org.eclipse.jdt.ui.JavaPerspective";
  public static final String ID_DEBUG_PERSPECTIVE = "org.eclipse.debug.ui.DebugPerspective";
  public static final String ID_RESOURCE_PERSPECTIVE = "eclipse.ui.resourcePerspective";

  /* *********************************************
   *
   * Multiused label name.
   *
   * ********************************************
   */

  public static final String ADD_PROJECTS = get("add_projects");

  /* *********************************************
   *
   * Main Menu Saros
   *
   * ********************************************
   */

  // the labels are defined in the plugin.xml so we use our own property file
  public static final String MENU_SAROS = get("menu_saros");
  public static final String MENU_CREATE_ACCOUNT = get("menu_create_account");
  public static final String MENU_ADD_CONTACT = get("menu_add_contact");
  public static final String MENU_ADD_CONTACTS_TO_SESSION = get("menu_add_contacts_to_session");
  public static final String SHARE_PROJECTS = get("share_projects");
  public static final String MENU_PREFERENCES = get("menu_preferences");
  public static final String MENU_STOP_SESSION = get("menu_stop_session");

  /* *********************************************
   *
   * Saros Preferences
   *
   * *********************************************
   */
  public static final String SHELL_SAROS_CONFIGURATION = get("shell_saros_configuration");

  public static final String SHELL_PREFERNCES = get("shell_preferences");

  public static final String SHELL_CREATE_XMPP_JABBER_ACCOUNT =
      get("shell_create_xmpp_jabber_account");
  public static final String SHELL_ADD_XMPP_JABBER_ACCOUNT = get("shell_add_xmpp_jabber_account");
  public static final String SHELL_EDIT_XMPP_JABBER_ACCOUNT = get("shell_edit_xmpp_jabber_account");

  public static final String CHATROOM_TAB_LABEL = get("ChatRoomsComposite_roundtable");

  public static final String LABEL_XMPP_JABBER_ID = get("jid_shortform");
  public static final String LABEL_XMPP_JABBER_SERVER = get("text_label_xmpp_jabber_server");
  public static final String LABEL_USER_NAME = get("text_label_user_name");
  public static final String LABEL_PASSWORD = get("text_label_password");
  public static final String LABEL_REPEAT_PASSWORD = get("text_label_repeat_password");

  public static final String ResourceSelectionComposite_delete_dialog_title =
      get("ResourceSelectionComposite_delete_dialog_title");
  public static final String ResourceSelectionComposite_overwrite_dialog_title =
      get("ResourceSelectionComposite_overwrite_dialog_title");

  public static final String ERROR_MESSAGE_PASSWORDS_NOT_MATCH =
      get("CreateXMPPAccountWizardPage_error_password_no_match");
  public static final String ERROR_MESSAGE_COULD_NOT_CONNECT =
      get("error_message_could_not_connect");
  public static final String ERROR_MESSAGE_NOT_CONNECTED_TO_SERVER =
      get("error_message_not_connected_to_server");
  public static final String ERROR_MESSAGE_ACCOUNT_ALREADY_EXISTS =
      get("account_exists_errorMessage");
  public static final String ERROR_MESSAGE_TOO_FAST_REGISTER_ACCOUNTS =
      get("error_message_too_fast_register_accounts");
  public static final String GROUP_EXISTING_ACCOUNT = get("group_existing_account");
  public static final String GROUP_TITLE_XMPP_JABBER_ACCOUNTS =
      get("group_title_xmpp_jabber_accounts_in_shell-saros-preferences");
  public static final String LABEL_ACTIVE_ACCOUNT_PREFIX = get("GeneralPreferencePage_active");
  public static final String BUTTON_ACTIVATE_ACCOUNT =
      get("button_text_activate_account_in_shell-saros-preferences");
  public static final String BUTTON_EDIT_ACCOUNT = get("button_edit_account");
  public static final String BUTTON_ADD_ACCOUNT =
      get("button_text_add_account_in_shell-saros-preferences");
  public static final String BUTTON_REMOVE_ACCOUNT = get("GeneralPreferencePage_REMOVE_BTN_TEXT");
  public static final String CHECKBOX_AUTO_CONNECT_ON_STARTUP =
      get("checkbox_label_auto_connect_on_startup_in_shell-saros-preferences");
  public static final String CHECKBOX_DISABLE_VERSION_CONTROL =
      get("checkbox_label_disable_version_control_support_in_shell-saros-preferences");
  public static final String CHECKBOX_ENABLE_CONCURRENT_UNDO =
      get("checkbox_label_enable_concurrent_undo_shell-saros-preferences");
  public static final String CHECKBOX_START_FOLLOW_MODE =
      get("checkbox_label_start_in_follow_mode_in_shell-saros-preferences");
  public static final String REMOVE_ACCOUNT_DIALOG_TITLE =
      get("GeneralPreferencePage_REMOVE_ACCOUNT_DIALOG_TITLE");
  public static final String ACTIVATE_ACCOUNT_DIALOG_TITLE =
      get("GeneralPreferencePage_ACTIVATE_ACCOUNT_DIALOG_TITLE");

  public static final String NODE_SAROS = get("node_saros");

  public static final String NODE_SAROS_FEEDBACK = get("node_Saros_feedback");

  public static final String NODE_SAROS_ADVANCED = get("node_Saros_Advanced");
  public static final String PREF_NODE_SAROS_ADVANCED_INSTANT_SESSION_START_PREFERRED =
      get("AdvancedPreferencePage_instant_session_start_preferred");

  public static final String NODE_SAROS_COMMUNICATION = get("node_Saros_Communication");

  public static final String NODE_SAROS_NETWORK = get("node_saros_network");
  public static final String PREF_NODE_SAROS_NETWORK_TRANSPORT_MODE_GROUP =
      get("NetworkPreferencePage_connection_established");
  public static final String PREF_NODE_SAROS_NETWORK_TRANSPORT_MODE_SOCKS5_MEDIATED_CHECKBOX =
      get("NetworkPreferencePage_buttonOnlyAllowMediatedSocks5_text");
  public static final String PREF_NODE_SAROS_NETWORK_TRANSPORT_MODE_IBB_CHECKBOX =
      get("NetworkPreferencePage_button_establish_connection");
  public static final String PREF_NODE_SAROS_NETWORK_ADVANCED_GROUP_FILE_TRANSFER =
      get("saros_advanced_group_file_transfer");
  public static final String PREF_NODE_SAROS_NETWORK_ADVANCED_GROUP_FILE_TRANSFER_FORCE_IBB =
      get("saros_advanced_group_file_transfer_force_ibb");

  /* *********************************************
   *
   * Saros View
   *
   * ********************************************
   */

  /*
   * View infos
   */
  public static final String VIEW_SAROS = get("view_saros");
  public static final String VIEW_SAROS_ID = get("view_saros_id");
  public static final String VIEW_SAROS_WHITEBOARD = get("view_saros_whiteboard");
  public static final String VIEW_SAROS_WHITEBOARD_ID = get("view_saros_whiteboard_id");
  public static final String SHELL_REQUEST_OF_SUBSCRIPTION_RECEIVED =
      get("SubscriptionManager_incoming_subscription_request_title");
  public static final String SHELL_SERVER_NOT_FOUND = get("shell_server_not_found");
  public static final String SHELL_SET_NEW_NICKNAME =
      get("RenameContactAction_new_nickname_dialog_title");
  public static final String SHELL_CONFIRM_CLOSING_SESSION = get("shell_confirm_closing_session");
  public static final String SHELL_ADD_CONTACT_WIZARD = get("AddContactWizard_title");

  /* *************************************************************
   * Saros View Context Menus
   * *************************************************************
   */

  /* **************************** session ************************* */
  public static final String CM_ADD_CONTACTS_TO_SESSION = get("cm_add_contacts_to_session");
  public static final String CM_GRANT_WRITE_ACCESS = get("cm_grant_write_access");
  public static final String CM_RESTRICT_TO_READ_ONLY_ACCESS =
      get("cm_restrict_to_read_only_access");
  public static final String CM_FOLLOW_PARTICIPANT = get("cm_follow_participant");
  public static final String CM_STOP_FOLLOWING = get("cm_stop_following");
  public static final String CM_JUMP_TO_POSITION_OF_PARTICIPANT =
      get("cm_jump_to_position_of_participant");
  public static final String CM_CHANGE_COLOR = get("cm_change_color");
  public static final String CM_STOP_SAROS_SESSION = get("cm_stop_saros_session");

  /* ************************** contact list ********************** */
  public static final String CM_ADD_CONTACT = get("cm_add_contact");
  public static final String CM_DELETE = get("cm_delete");
  public static final String CM_RENAME = get("cm_rename");
  public static final String CM_OPEN_CHAT = get("OpenChatAction_MenuItem");
  public static final String CM_ADD_TO_SAROS_SESSION = get("cm_add_to_saros_session");

  /* ***************** package explorer view ********************* */
  public static final String CM_SHARE_WITH = get("cm_share_with");
  public static final String CM_WORK_TOGETHER_ON = get("cm_work_together_on");
  public static final String CM_MULTIPLE_CONTACTS = get("cm_multiple_contacts");
  public static final String CM_MULTIPLE_PROJECTS = get("cm_multiple_projects");

  /* *************************************************************** */

  public static final String SHELL_ADD_CONTACT_TO_SESSION = get("SessionAddContactsWizard_title");
  public static final String SHELL_ERROR_IN_SAROS_PLUGIN = get("shell_error_in_saros_plugin");
  public static final String SHELL_CLOSING_THE_SESSION = get("close_the_session");
  public static final String SHELL_CONFIRM_LEAVING_SESSION = get("confirm_leaving_session");
  public static final String SHELL_CONFIRM_DECLINE_INVITATION =
      get("shell_confirm_decline_invitation");
  public static final String SHELL_CONFIRM_CONSISTENCY_RECOVERY =
      get("ConsistencyAction_confirm_dialog_title");

  /* *************************************************************
   * Saros View Toolbar Buttons
   * *************************************************************
   */

  public static final String TB_DISCONNECT = get("tb_disconnect");
  public static final String TB_ADD_NEW_CONTACT = get("NewContactAction_tooltip");
  public static final String TB_CONNECT = get("tb_connect");
  public static final String TB_SEND_A_FILE_TO_SELECTED_CONTACT =
      get("tb_send_a_file_to_selected_contact");
  public static final String TB_NO_INCONSISTENCIES = get("tb_no_inconsistencies");
  public static final String TB_INCONSISTENCY_DETECTED = get("tb_inconsistency_detected_in");
  public static final String TB_RESTRICT_INVITEES_TO_READ_ONLY_ACCESS =
      get("tb_restrict_invitees_to_read_only_access");
  public static final String TB_ENABLE_DISABLE_FOLLOW_MODE = get("tb_enable_disable_follow_mode");
  public static final String TB_LEAVE_SESSION = get("LeaveSessionAction_leave_session_tooltip");
  public static final String TB_STOP_SESSION = get("LeaveSessionAction_stop_session_tooltip");
  public static final String NODE_CONTACTS = get("RosterHeaderElement_contacts");
  public static final String NODE_SESSION = get("SessionHeaderElement_session");
  public static final String NODE_NO_SESSION_RUNNING =
      get("SessionHeaderElement_no_session_running");
  public static final String HOST_INDICATION = get("UserElement_host");
  public static final String GROUP_TITLE_CREATE_NEW_XMPP_JABBER_ACCOUNT =
      get("group_title_create_new_xmpp_jabber_account");

  // Permission: Write Access
  public static final String READ_ONLY_ACCESS = get("read_only_access");
  public static final String FOLLOW_MODE_ENABLED = get("follow_mode_enabled");
  public static final String FOLLOW_MODE_PAUSED = get("follow_mode_paused");

  /* *********************************************
   *
   * Context Menu Saros
   *
   * ********************************************
   */

  public static final int CREATE_NEW_PROJECT = 1;
  public static final int USE_EXISTING_PROJECT = 2;
  public static final int USE_EXISTING_PROJECT_WITH_CANCEL_LOCAL_CHANGE = 3;
  public static final int USE_EXISTING_PROJECT_WITH_COPY = 4;

  /*
   * title of shells which are pop up by performing the actions on the package
   * explorer view.
   */
  public static final String SHELL_INVITATION_CANCELED = get("shell_invitation_canceled");
  public static final String SHELL_SESSION_INVITATION = get("shell_session_invitation");
  public static final String SHELL_ADD_PROJECTS = get("shell_add_projects");
  public static final String SHELL_ADD_PROJECTS_TO_SESSION = get("shell_add_projects_to_session");
  public static final String SHELL_PROBLEM_OCCURRED = get("shell_problem_occurred");
  public static final String SHELL_WARNING_LOCAL_CHANGES_DELETED =
      get("shell_warning_local_changes_deleted");
  public static final String SHELL_FOLDER_SELECTION = get("shell_folder_selection");
  public static final String SHELL_SAVE_ALL_FILES_NOW = get("shell_save_all_files_now");
  public static final String SHELL_NEW_FILE_SHARED = get("shell_new_file_shared");
  public static final String SHELL_NEED_BASED_SYNC = get("shell_need_based_sync");

  public static final String SHELL_CONFIRM_SAVE_UNCHANGED_CHANGES =
      get("AddProjectToSessionWizard_unsaved_changes_dialog_title");

  /*
   * second page of the wizard "Session invitation"
   */
  public static final String RADIO_USING_EXISTING_PROJECT = get("radio_use_existing_project");
  public static final String RADIO_CREATE_NEW_PROJECT = get("radio_create_new_project");

  /* *********************************************
   *
   * ContextMenu: Open/Open With
   *
   * ********************************************
   */
  /* Context menu of a selected file on the package explorer view */
  public static final String CM_OPEN = "Open";
  public static final String CM_OPEN_WITH = "Open With";
  public static final String CM_OTHER = "Other...";
  public static final String CM_OPEN_WITH_TEXT_EDITOR = "Text Editor";
  public static final String CM_OPEN_WITH_SYSTEM_EDITOR = "System Editor";

  /* *********************************************
   *
   * STFBotEditor
   *
   * ********************************************
   */

  public static final String ID_JAVA_EDITOR = "org.eclipse.jdt.ui.CompilationUnitEditor";
  public static final String ID_TEXT_EDITOR = "org.eclipse.ui.texteditor";

  /* Title of shells */
  public static String SHELL_SAVE_RESOURCE = "Save Resource";

  /* *********************************************
   *
   * View Package Explorer
   *
   * ********************************************
   */

  public static final String VIEW_PACKAGE_EXPLORER = "Package Explorer";
  public static final String VIEW_PACKAGE_EXPLORER_ID = "org.eclipse.jdt.ui.PackageExplorer";

  public static final String SHELL_EDITOR_SELECTION = "Editor Selection";

  public static final String TB_COLLAPSE_ALL = "Collapse All.*";

  /* *********************************************
   *
   * ContextMenu: Run As
   *
   * ********************************************
   */
  /* Context menu of a selected item on the package explorer view */
  public static final String CM_RUN_AS = "Run As";
  public static final String CM_ON_SERVER = "Run on server";
  public static final String CM_JAVA_APPLET = "Java Applet";
  public static final String CM_JAVA_APPLICATION = "Java Application";

  /* *********************************************
   *
   * View Progress
   *
   * ********************************************
   */
  public static final String VIEW_PROGRESS = "Progress";
  public static final String VIEW_PROGRESS_ID = "org.eclipse.ui.views.ProgressView";

  public static final String TB_REMOVE_ALL_FINISHED_OPERATIONS = "Remove All Finished Operations";

  /* *********************************************
   *
   * View Console
   *
   * ********************************************
   */
  public static final String VIEW_CONSOLE = "Console";
  public static final String VIEW_CONSOLE_ID = "org.eclipse.ui.console.ConsoleView";

  /* *********************************************
   *
   * Context Menu Team
   *
   * ********************************************
   */
  public static final String SHELL_SHARE_PROJECT = "Share Project";

  /*
   * Invit. / Synch. Monitor Names
   */

  public static final String SHELL_MONITOR_PROJECT_SYNCHRONIZATION =
      get("shell_monitor_project_synchronization");
  /* *********************************************
   *
   * Others
   *
   * ********************************************
   */
  public static final String STRING_REGEX_WITH_LINE_BREAK = ".*\n*.*";
  public static final String PKG_REGEX = "[\\w*\\.]*\\w*";
  public static final String PROJECT_REGEX = "\\w*";
}
