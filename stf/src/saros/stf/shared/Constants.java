package saros.stf.shared;

import static saros.stf.shared.Configuration.get;

public interface Constants {

  /** Defines how invitees are added to the session. */
  enum SessionInvitationModality {
    SEQUENTIALLY,
    CONCURRENTLY
  }

  /**
   * Defines how the shared project is represented on the invitee's side.
   *
   * <p><b>Note:</b> Using creation type {@link TypeOfCreateProject#NEW_PROJECT} does not enforce
   * that the project nature on the inviter's side is correctly applied on the invitee's side. As a
   * result, it is only supported for cases where the project nature is not of interest. In cases
   * where the same project nature is necessary on all sides (e.g. if Java language support is
   * needed), please use {@link TypeOfCreateProject#EXIST_PROJECT} instead.
   */
  enum TypeOfCreateProject {
    NEW_PROJECT,
    EXIST_PROJECT,
    EXIST_PROJECT_WITH_COPY
  }

  enum TypeOfShareProject {
    SHARE_PROJECT,
    SHARE_PROJECT_PARTICALLY,
    ADD_SESSION
  }

  enum TreeItemType {
    JAVA_PROJECT,
    PROJECT,
    FILE,
    CLASS,
    PKG,
    FOLDER,
    NULL
  }

  String CONFIRM_DELETE = "Confirm Delete";
  String DELETE = "Delete";
  String SHELL_COPY_PROJECT = "Copy Project";
  String SHELL_DELETING_ACTIVE_ACCOUNT = "Deleting active account";

  /*
   * DECORATORS
   */

  String PROJECT_SHARED_DECORATOR = get("SharedProjectDecorator_shared");

  /* *********************************************
   *
   * Basic Wigets
   *
   * ********************************************
   */
  // Title of Buttons
  String YES = "Yes";
  String OK = "OK";
  String APPLY_AND_CLOSE = "Apply and Close";
  String NO = "No";
  String CANCEL = "Cancel";
  String FINISH = "Finish";
  String ACCEPT = "Accept";
  String APPLY = "Apply";
  String NEXT = "Next >";
  String BACK = "< Back";
  String BROWSE = "Browse";
  String OVERWRITE = "Overwrite";
  String BACKUP = "Create Backup";
  String RUN_IN_BACKGROUND = "Run in Background";
  String RESTORE_DEFAULTS = "Restore Defaults";

  String SRC = "src";
  String SUFFIX_JAVA = ".java";

  /* *********************************************
   *
   * View Progress
   *
   * ********************************************
   */
  String SHELL_PROGRESS_INFORMATION = "Progress Information";

  /* *********************************************
   *
   * Dialog Preferences
   *
   * ********************************************
   */
  String NODE_CONSOLE = "Console";
  String NODE_EDITORS = "Editors";
  String NODE_TEXT_EDITORS = "Text Editors";
  String NODE_ANNOTATIONS = "Annotations";
  /* *********************************************
   *
   * Main Menu File
   *
   * ********************************************
   */

  String MENU_NEW = "New";
  String MENU_PROJECT = "Project...";
  String MENU_FOLDER = "Folder";
  String MENU_FILE = "File";
  String MENU_CLASS = "Class";
  String MENU_PACKAGE = "Package";
  String MENU_JAVA_PROJECT = "Java Project";
  String MENU_CLOSE = "Close";
  String MENU_CLOSE_ALL = "Close All";
  String MENU_REFRESH = "Refresh";
  String MENU_SAVE = "Save";
  String MENU_SAVE_AS = "Save As...";
  String MENU_SAVE_All = "Save All";

  String SHELL_NEW_FOLDER = "New Folder";
  String SHELL_NEW_FILE = "New File";
  String SHELL_NEW_JAVA_PACKAGE = "New Java Package";
  String SHELL_NEW_JAVA_CLASS = "New Java Class";
  String SHELL_NEW_PROJECT = "New Project";
  String SHELL_NEW_JAVA_PROJECT = "New Java Project";

  /* categories and nodes of the shell "New Project" */
  String NODE_GENERAL = "General";
  String NODE_PROJECT = "Project";

  /* Eclipse Project Wizard */
  String LABEL_PROJECT_NAME = "Project name:";
  String LABEL_FILE_NAME = "File name:";
  String LABEL_FOLDER_NAME = "Folder name:";
  String LABEL_ENTER_OR_SELECT_THE_PARENT_FOLDER = "Enter or select the parent folder:";
  String LABEL_SOURCE_FOLDER = "Source folder:";
  String LABEL_PACKAGE = "Package:";
  String LABEL_NAME = "Name:";

  /* *********************************************
   *
   * Main Menu Edit
   *
   * ********************************************
   */
  String SHELL_DELETE_RESOURCE = "Delete Resources";

  /* menu names */
  String MENU_DELETE = "Delete";
  String MENU_EDIT = "Edit";
  String MENU_COPY = "Copy";
  String MENU_PASTE = "Paste";

  /* *********************************************
   *
   * Main Menu Refactor
   *
   * ********************************************
   */
  /* shell titles */
  String SHELL_MOVE = "Move";
  String SHELL_RENAME_PACKAGE = "Rename Package";
  String SHELL_RENAME_JAVA_PROJECT = "Rename Java Project";
  String SHELL_RENAME_RESOURCE = "Rename Resource";
  String SHELL_RENAME_COMPIIATION_UNIT = "Rename Compilation Unit";
  String LABEL_NEW_NAME = "New name:";

  /* menu names */
  String MENU_REFACTOR = "Refactor";
  String MENU_RENAME = "Rename...";
  String MENU_MOVE = "Move...";

  /* *********************************************
   *
   * Main Menu Window
   *
   * ********************************************
   */

  String TREE_ITEM_GENERAL_IN_PRFERENCES = "General";
  String TREE_ITEM_WORKSPACE_IN_PREFERENCES = "Workspace";
  String TREE_ITEM_GENERAL_IN_SHELL_SHOW_VIEW = "General";
  String TREE_ITEM_PROBLEM_IN_SHELL_SHOW_VIEW = "Problems";
  String TREE_ITEM_PROJECT_EXPLORER_IN_SHELL_SHOW_VIEW = "Project Explorer";

  /* name of all the main menus */
  String MENU_WINDOW = "Window";
  String MENU_OTHER = "Other...";
  String MENU_SHOW_VIEW = "Show View";

  String SHELL_SHOW_VIEW = "Show View";

  /* IDs of all the perspectives */
  String ID_JAVA_PERSPECTIVE = "org.eclipse.jdt.ui.JavaPerspective";
  String ID_DEBUG_PERSPECTIVE = "org.eclipse.debug.ui.DebugPerspective";
  String ID_RESOURCE_PERSPECTIVE = "eclipse.ui.resourcePerspective";

  /* *********************************************
   *
   * Multiused label name.
   *
   * ********************************************
   */

  String ADD_RESOURCES = get("add_resources");

  /* *********************************************
   *
   * Main Menu Saros
   *
   * ********************************************
   */

  // the labels are defined in the plugin.xml so we use our own property file
  String MENU_SAROS = get("menu_saros");
  String MENU_CREATE_ACCOUNT = get("menu_create_account");
  String MENU_ADD_CONTACT = get("menu_add_contact");
  String MENU_ADD_CONTACTS_TO_SESSION = get("menu_add_contacts_to_session");
  String SHARE_RESOURCES = get("share_resources");
  String MENU_PREFERENCES = get("menu_preferences");
  String MENU_STOP_SESSION = get("menu_stop_session");

  /* *********************************************
   *
   * Saros Preferences
   *
   * *********************************************
   */
  String SHELL_SAROS_CONFIGURATION = get("shell_saros_configuration");

  String SHELL_PREFERNCES = get("shell_preferences");

  String SHELL_CREATE_XMPP_JABBER_ACCOUNT = get("shell_create_xmpp_jabber_account");
  String SHELL_ADD_XMPP_JABBER_ACCOUNT = get("shell_add_xmpp_jabber_account");
  String SHELL_EDIT_XMPP_JABBER_ACCOUNT = get("shell_edit_xmpp_jabber_account");

  String CHATROOM_TAB_LABEL = get("ChatRoomsComposite_roundtable");

  String LABEL_XMPP_JABBER_ID = get("jid_shortform");
  String LABEL_XMPP_JABBER_SERVER = get("text_label_xmpp_jabber_server");
  String LABEL_USER_NAME = get("text_label_user_name");
  String LABEL_PASSWORD = get("text_label_password");
  String LABEL_REPEAT_PASSWORD = get("text_label_repeat_password");

  String ResourceSelectionComposite_delete_dialog_title =
      get("ResourceSelectionComposite_delete_dialog_title");
  String ResourceSelectionComposite_overwrite_dialog_title =
      get("ResourceSelectionComposite_overwrite_dialog_title");

  String ERROR_MESSAGE_PASSWORDS_NOT_MATCH =
      get("CreateXMPPAccountWizardPage_error_password_no_match");
  String ERROR_MESSAGE_COULD_NOT_CONNECT = get("error_message_could_not_connect");
  String ERROR_MESSAGE_NOT_CONNECTED_TO_SERVER = get("error_message_not_connected_to_server");
  String ERROR_MESSAGE_ACCOUNT_ALREADY_EXISTS = get("account_exists_errorMessage");
  String ERROR_MESSAGE_TOO_FAST_REGISTER_ACCOUNTS = get("error_message_too_fast_register_accounts");
  String GROUP_EXISTING_ACCOUNT = get("group_existing_account");
  String GROUP_TITLE_XMPP_JABBER_ACCOUNTS =
      get("group_title_xmpp_jabber_accounts_in_shell-saros-preferences");
  String LABEL_ACTIVE_ACCOUNT_PREFIX = get("GeneralPreferencePage_active");
  String BUTTON_ACTIVATE_ACCOUNT = get("button_text_activate_account_in_shell-saros-preferences");
  String BUTTON_EDIT_ACCOUNT = get("button_edit_account");
  String BUTTON_ADD_ACCOUNT = get("button_text_add_account_in_shell-saros-preferences");
  String BUTTON_REMOVE_ACCOUNT = get("GeneralPreferencePage_REMOVE_BTN_TEXT");
  String CHECKBOX_AUTO_CONNECT_ON_STARTUP =
      get("checkbox_label_auto_connect_on_startup_in_shell-saros-preferences");
  String CHECKBOX_DISABLE_VERSION_CONTROL =
      get("checkbox_label_disable_version_control_support_in_shell-saros-preferences");
  String CHECKBOX_ENABLE_CONCURRENT_UNDO =
      get("checkbox_label_enable_concurrent_undo_shell-saros-preferences");
  String CHECKBOX_START_FOLLOW_MODE =
      get("checkbox_label_start_in_follow_mode_in_shell-saros-preferences");
  String REMOVE_ACCOUNT_DIALOG_TITLE = get("GeneralPreferencePage_REMOVE_ACCOUNT_DIALOG_TITLE");
  String ACTIVATE_ACCOUNT_DIALOG_TITLE = get("GeneralPreferencePage_ACTIVATE_ACCOUNT_DIALOG_TITLE");

  String NODE_SAROS = get("node_saros");

  String NODE_SAROS_FEEDBACK = get("node_Saros_feedback");

  String NODE_SAROS_ADVANCED = get("node_Saros_Advanced");
  String PREF_NODE_SAROS_ADVANCED_INSTANT_SESSION_START_PREFERRED =
      get("AdvancedPreferencePage_instant_session_start_preferred");

  String NODE_SAROS_COMMUNICATION = get("node_Saros_Communication");

  String NODE_SAROS_NETWORK = get("node_saros_network");
  String PREF_NODE_SAROS_NETWORK_TRANSPORT_MODE_GROUP =
      get("NetworkPreferencePage_connection_established");
  String PREF_NODE_SAROS_NETWORK_TRANSPORT_MODE_SOCKS5_MEDIATED_CHECKBOX =
      get("NetworkPreferencePage_buttonOnlyAllowMediatedSocks5_text");
  String PREF_NODE_SAROS_NETWORK_TRANSPORT_MODE_IBB_CHECKBOX =
      get("NetworkPreferencePage_button_establish_connection");
  String PREF_NODE_SAROS_NETWORK_ADVANCED_GROUP_FILE_TRANSFER =
      get("saros_advanced_group_file_transfer");
  String PREF_NODE_SAROS_NETWORK_ADVANCED_GROUP_FILE_TRANSFER_FORCE_IBB =
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
  String VIEW_SAROS = get("view_saros");
  String VIEW_SAROS_ID = get("view_saros_id");
  String SHELL_REQUEST_OF_SUBSCRIPTION_RECEIVED =
      get("SubscriptionManager_incoming_subscription_request_title");
  String SHELL_SERVER_NOT_FOUND = get("shell_server_not_found");
  String SHELL_SET_NEW_NICKNAME = get("RenameContactAction_new_nickname_dialog_title");
  String SHELL_CONFIRM_CLOSING_SESSION = get("shell_confirm_closing_session");
  String SHELL_ADD_CONTACT_WIZARD = get("AddContactWizard_title");

  /* *************************************************************
   * Saros View Context Menus
   * *************************************************************
   */

  /* **************************** session ************************* */
  String CM_ADD_CONTACTS_TO_SESSION = get("cm_add_contacts_to_session");
  String CM_GRANT_WRITE_ACCESS = get("cm_grant_write_access");
  String CM_RESTRICT_TO_READ_ONLY_ACCESS = get("cm_restrict_to_read_only_access");
  String CM_FOLLOW_PARTICIPANT = get("cm_follow_participant");
  String CM_STOP_FOLLOWING = get("cm_stop_following");
  String CM_JUMP_TO_POSITION_OF_PARTICIPANT = get("cm_jump_to_position_of_participant");
  String CM_CHANGE_COLOR = get("cm_change_color");
  String CM_STOP_SAROS_SESSION = get("cm_stop_saros_session");

  /* ************************** contact list ********************** */
  String CM_ADD_CONTACT = get("cm_add_contact");
  String CM_DELETE = get("cm_delete");
  String CM_RENAME = get("cm_rename");
  String CM_OPEN_CHAT = get("OpenChatAction_MenuItem");
  String CM_ADD_TO_SAROS_SESSION = get("cm_add_to_saros_session");

  /* ***************** package explorer view ********************* */
  String CM_SHARE_WITH = get("cm_share_with");
  String CM_WORK_TOGETHER_ON = get("cm_work_together_on");
  String CM_MULTIPLE_CONTACTS = get("cm_multiple_contacts");
  String CM_MULTIPLE_RESOURCE_ROOTS = get("cm_multiple_resource_roots");

  /* *************************************************************** */

  String SHELL_ADD_CONTACT_TO_SESSION = get("SessionAddContactsWizard_title");
  String SHELL_ERROR_IN_SAROS_PLUGIN = get("shell_error_in_saros_plugin");
  String SHELL_CLOSING_THE_SESSION = get("close_the_session");
  String SHELL_CONFIRM_LEAVING_SESSION = get("confirm_leaving_session");
  String SHELL_CONFIRM_DECLINE_INVITATION = get("shell_confirm_decline_invitation");
  String SHELL_CONFIRM_CONSISTENCY_RECOVERY = get("ConsistencyAction_confirm_dialog_title");

  /* *************************************************************
   * Saros View Toolbar Buttons
   * *************************************************************
   */

  String TB_DISCONNECT = get("tb_disconnect");
  String TB_ADD_NEW_CONTACT = get("NewContactAction_tooltip");
  String TB_CONNECT = get("tb_connect");
  String TB_SEND_A_FILE_TO_SELECTED_CONTACT = get("tb_send_a_file_to_selected_contact");
  String TB_NO_INCONSISTENCIES = get("tb_no_inconsistencies");
  String TB_INCONSISTENCY_DETECTED = get("tb_inconsistency_detected_in");
  String TB_RESTRICT_INVITEES_TO_READ_ONLY_ACCESS = get("tb_restrict_invitees_to_read_only_access");
  String TB_ENABLE_DISABLE_FOLLOW_MODE = get("tb_enable_disable_follow_mode");
  String TB_LEAVE_SESSION = get("LeaveSessionAction_leave_session_tooltip");
  String TB_STOP_SESSION = get("LeaveSessionAction_stop_session_tooltip");
  String NODE_CONTACTS = get("RosterHeaderElement_contacts");
  String NODE_SESSION = get("SessionHeaderElement_session");
  String NODE_NO_SESSION_RUNNING = get("SessionHeaderElement_no_session_running");
  String HOST_INDICATION = get("UserElement_host");
  String GROUP_TITLE_CREATE_NEW_XMPP_JABBER_ACCOUNT =
      get("group_title_create_new_xmpp_jabber_account");

  // Permission: Write Access
  String READ_ONLY_ACCESS = get("read_only_access");
  String FOLLOW_MODE_ENABLED = get("follow_mode_enabled");
  String FOLLOW_MODE_PAUSED = get("follow_mode_paused");

  /* *********************************************
   *
   * Context Menu Saros
   *
   * ********************************************
   */

  int CREATE_NEW_PROJECT = 1;
  int USE_EXISTING_PROJECT = 2;
  int USE_EXISTING_PROJECT_WITH_CANCEL_LOCAL_CHANGE = 3;
  int USE_EXISTING_PROJECT_WITH_COPY = 4;

  /*
   * title of shells which are pop up by performing the actions on the package
   * explorer view.
   */
  String SHELL_INVITATION_CANCELED = get("shell_invitation_canceled");
  String SHELL_SESSION_INVITATION = get("shell_session_invitation");
  String SHELL_ADD_RESOURCES = get("shell_add_resources");
  String SHELL_ADD_PROJECTS_TO_SESSION = get("shell_add_projects_to_session");
  String SHELL_PROBLEM_OCCURRED = get("shell_problem_occurred");
  String SHELL_WARNING_LOCAL_CHANGES_DELETED = get("shell_warning_local_changes_deleted");
  String SHELL_FOLDER_SELECTION = get("shell_folder_selection");
  String SHELL_SAVE_ALL_FILES_NOW = get("shell_save_all_files_now");
  String SHELL_NEW_FILE_SHARED = get("shell_new_file_shared");
  String SHELL_NEED_BASED_SYNC = get("shell_need_based_sync");

  String SHELL_CONFIRM_SAVE_UNCHANGED_CHANGES =
      get("AddReferencePointsToSessionWizard_unsaved_changes_dialog_title");

  /*
   * second page of the wizard "Session invitation"
   */
  String RADIO_USING_EXISTING_DIRECTORY = get("radio_use_existing_directory");
  String RADIO_CREATE_NEW_PROJECT = get("radio_create_new_project");
  String LABEL_NEW_PROJECT_NAME = get("label_new_project_name");
  String LABEL_EXISTING_DIRECTORY_PATH = get("label_existing_directory_path");

  /* *********************************************
   *
   * ContextMenu: Open/Open With
   *
   * ********************************************
   */
  /* Context menu of a selected file on the package explorer view */
  String CM_OPEN = "Open";
  String CM_OPEN_WITH = "Open With";
  String CM_OTHER = "Other...";
  String CM_OPEN_WITH_TEXT_EDITOR = "Text Editor";
  String CM_OPEN_WITH_SYSTEM_EDITOR = "System Editor";

  /* *********************************************
   *
   * STFBotEditor
   *
   * ********************************************
   */

  String ID_JAVA_EDITOR = "org.eclipse.jdt.ui.CompilationUnitEditor";
  String ID_TEXT_EDITOR = "org.eclipse.ui.texteditor";

  /* Title of shells */
  String SHELL_SAVE_RESOURCE = "Save Resource";

  /* *********************************************
   *
   * View Package Explorer
   *
   * ********************************************
   */

  String VIEW_PACKAGE_EXPLORER = "Package Explorer";
  String VIEW_PACKAGE_EXPLORER_ID = "org.eclipse.jdt.ui.PackageExplorer";

  String SHELL_EDITOR_SELECTION = "Editor Selection";

  String TB_COLLAPSE_ALL = "Collapse All.*";

  /* *********************************************
   *
   * ContextMenu: Run As
   *
   * ********************************************
   */
  /* Context menu of a selected item on the package explorer view */
  String CM_RUN_AS = "Run As";
  String CM_ON_SERVER = "Run on server";
  String CM_JAVA_APPLET = "Java Applet";
  String CM_JAVA_APPLICATION = "Java Application";

  /* *********************************************
   *
   * View Progress
   *
   * ********************************************
   */
  String VIEW_PROGRESS = "Progress";
  String VIEW_PROGRESS_ID = "org.eclipse.ui.views.ProgressView";

  String TB_REMOVE_ALL_FINISHED_OPERATIONS = "Remove All Finished Operations";

  /* *********************************************
   *
   * View Console
   *
   * ********************************************
   */
  String VIEW_CONSOLE = "Console";
  String VIEW_CONSOLE_ID = "org.eclipse.ui.console.ConsoleView";

  /* *********************************************
   *
   * Context Menu Team
   *
   * ********************************************
   */
  String SHELL_SHARE_PROJECT = "Share Project";

  /*
   * Invit. / Synch. Monitor Names
   */

  String SHELL_MONITOR_PROJECT_SYNCHRONIZATION = get("shell_monitor_project_synchronization");
  /* *********************************************
   *
   * Others
   *
   * ********************************************
   */
  String STRING_REGEX_WITH_LINE_BREAK = ".*\n*.*";
  String PKG_REGEX = "[\\w*\\.]*\\w*";
  String PROJECT_REGEX = "\\w*";
}
