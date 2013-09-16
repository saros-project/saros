package de.fu_berlin.inf.dpp.stf.shared;

// TODO sort by wizards / context menus instead of widget types !

public interface Constants {
    public enum TypeOfCreateProject {
        NEW_PROJECT, EXIST_PROJECT, EXIST_PROJECT_WITH_COPY
    }

    public enum TypeOfShareProject {
        SHARE_PROJECT, SHARE_PROJECT_PARTICALLY, ADD_SESSION
    }

    public enum TreeItemType {
        JAVA_PROJECT, PROJECT, FILE, CLASS, PKG, FOLDER, NULL
    }

    public enum BuddyRole {
        HOST, PEER
    }

    public final static String CONFIRM_DELETE = "Confirm Delete";
    public final static String SHELL_COPY_PROJECT = "Copy Project";
    public final static String SHELL_DELETING_ACTIVE_ACCOUNT = "Deleting active account";

    /*
     * DECORATORS
     */

    public final static String PROJECT_SHARED_DECORATOR = Configuration
        .getString("SharedProjectDecorator_shared");

    public final static String PROJECT_PARTIAL_SHARED_DECORATOR = Configuration
        .getString("SharedProjectDecorator_shared_partial");

    /**********************************************
     * 
     * Basic Widgets
     * 
     **********************************************/
    // Title of Buttons
    public final static String YES = "Yes";
    public final static String OK = "OK";
    public final static String NO = "No";
    public final static String CANCEL = "Cancel";
    public final static String FINISH = "Finish";
    public final static String ACCEPT = "Accept";
    public final static String APPLY = "Apply";
    public final static String NEXT = "Next >";
    public final static String BACK = "< Back";
    public final static String BROWSE = "Browse";
    public final static String OVERWRITE = "Overwrite";
    public final static String BACKUP = "Create Backup";
    public final static String RUN_IN_BACKGROUND = "Run in Background";
    public final static String RESTORE_DEFAULTS = "Restore Defaults";

    public final static String SRC = "src";
    public final static String SUFFIX_JAVA = ".java";

    /* *********************************************
     * 
     * View Progress
     * 
     * ********************************************
     */
    public final static String SHELL_PROGRESS_INFORMATION = "Progress Information";

    /* *********************************************
     * 
     * Dialog Preferences
     * 
     * ********************************************
     */
    static public final String NODE_CONSOLE = "Console";
    static public final String NODE_EDITORS = "Editors";
    static public final String NODE_TEXT_EDITORS = "Text Editors";
    static public final String NODE_ANNOTATIONS = "Annotations";
    /* *********************************************
     * 
     * Main Menu File
     * 
     * ********************************************
     */

    static public final String MENU_NEW = "New";
    static public final String MENU_PROJECT = "Project...";
    static public final String MENU_FOLDER = "Folder";
    static public final String MENU_FILE = "File";
    static public final String MENU_CLASS = "Class";
    static public final String MENU_PACKAGE = "Package";
    static public final String MENU_JAVA_PROJECT = "Java Project";
    static public final String MENU_CLOSE = "Close";
    static public final String MENU_CLOSE_ALL = "Close All";
    static public final String MENU_REFRESH = "Refresh";
    static public final String MENU_SAVE = "Save";
    static public final String MENU_SAVE_AS = "Save As...";
    static public final String MENU_SAVE_All = "Save All";

    public final static String SHELL_NEW_FOLDER = "New Folder";
    public final static String SHELL_NEW_FILE = "New File";
    public final static String SHELL_NEW_JAVA_PACKAGE = "New Java Package";
    public final static String SHELL_NEW_JAVA_CLASS = "New Java Class";
    static public final String SHELL_NEW_PROJECT = "New Project";
    static public final String SHELL_NEW_JAVA_PROJECT = "New Java Project";

    /* categories and nodes of the shell "New Project" */
    static public final String NODE_GENERAL = "General";
    static public final String NODE_PROJECT = "Project";

    /* Eclipse Project Wizard */
    static public final String LABEL_PROJECT_NAME = "Project name:";
    static public final String LABEL_FILE_NAME = "File name:";
    static public final String LABEL_FOLDER_NAME = "Folder name:";
    static public final String LABEL_ENTER_OR_SELECT_THE_PARENT_FOLDER = "Enter or select the parent folder:";
    static public final String LABEL_SOURCE_FOLDER = "Source folder:";
    static public final String LABEL_PACKAGE = "Package:";
    static public final String LABEL_NAME = "Name:";

    /* *********************************************
     * 
     * Main Menu Edit
     * 
     * ********************************************
     */
    public final static String SHELL_DELETE_RESOURCE = "Delete Resources";

    /* menu names */
    public final static String MENU_DELETE = "Delete";
    public final static String MENU_EDIT = "Edit";
    public final static String MENU_COPY = "Copy";
    public final static String MENU_PASTE = "Paste";

    /* *********************************************
     * 
     * Main Menu Refactor
     * 
     * ********************************************
     */
    /* shell titles */
    public final static String SHELL_MOVE = "Move";
    public final static String SHELL_RENAME_PACKAGE = "Rename Package";
    public final static String SHELL_RENAME_JAVA_PROJECT = "Rename Java Project";
    public final static String SHELL_RENAME_RESOURCE = "Rename Resource";
    public final static String SHELL_RENAME_COMPIIATION_UNIT = "Rename Compilation Unit";
    public final static String LABEL_NEW_NAME = "New name:";

    /* menu names */
    public final static String MENU_REFACTOR = "Refactor";
    public final static String MENU_RENAME = "Rename...";
    public final static String MENU_MOVE = "Move...";

    /* *********************************************
     * 
     * Main Menu Window
     * 
     * ********************************************
     */

    static public final String TREE_ITEM_GENERAL_IN_PRFERENCES = "General";
    static public final String TREE_ITEM_WORKSPACE_IN_PREFERENCES = "Workspace";
    static public final String TREE_ITEM_GENERAL_IN_SHELL_SHOW_VIEW = "General";
    static public final String TREE_ITEM_PROBLEM_IN_SHELL_SHOW_VIEW = "Problems";
    static public final String TREE_ITEM_PROJECT_EXPLORER_IN_SHELL_SHOW_VIEW = "Project Explorer";

    /* name of all the main menus */
    static public final String MENU_WINDOW = "Window";
    public final static String MENU_OTHER = "Other...";
    public final static String MENU_SHOW_VIEW = "Show View";

    static public final String SHELL_SHOW_VIEW = "Show View";

    /* IDs of all the perspectives */
    public final static String ID_JAVA_PERSPECTIVE = "org.eclipse.jdt.ui.JavaPerspective";
    public final static String ID_DEBUG_PERSPECTIVE = "org.eclipse.debug.ui.DebugPerspective";
    public final static String ID_RESOURCE_PERSPECTIVE = "eclipse.ui.resourcePerspective";

    /* *********************************************
     * 
     * Multiused label name.
     * 
     * ********************************************
     */

    public final static String ADD_PROJECTS = Configuration
        .getString("add_projects");

    /* *********************************************
     * 
     * Main Menu Saros
     * 
     * ********************************************
     */

    public final static String MENU_SAROS = Configuration
        .getString("menu_saros");
    public final static String MENU_START_SAROS_CONFIGURATION = Configuration
        .getString("menu_start_saros_configuration");
    public final static String MENU_CREATE_ACCOUNT = Configuration
        .getString("menu_create_account");
    public final static String MENU_ADD_BUDDY = Configuration
        .getString("menu_add_buddy");
    public final static String ADD_BUDDIES = Configuration
        .getString("add_buddies");
    public final static String SHARE_PROJECTS = Configuration
        .getString("share_projects");
    public final static String MENU_PREFERENCES = Configuration
        .getString("menu_preferences");
    public final static String MENU_STOP_SESSION = Configuration
        .getString("menu_stop_session");

    /* *********************************************
     * 
     * Saros Preferences
     * 
     * *********************************************
     */
    public final static String SHELL_PREFERNCES = Configuration
        .getString("shell_preferences");
    public final static String SHELL_CREATE_XMPP_JABBER_ACCOUNT = Configuration
        .getString("shell_create_xmpp_jabber_account");
    public final static String SHELL_SAROS_CONFIGURATION = Configuration
        .getString("shell_saros_configuration");

    public final static String SHELL_ADD_XMPP_JABBER_ACCOUNT = Configuration
        .getString("shell_add_xmpp_jabber_account");

    public final static String SHELL_EDIT_XMPP_JABBER_ACCOUNT = Configuration
        .getString("shell_edit_xmpp_jabber_account");

    public final static String CHATROOM_TAB_LABEL = Configuration
        .getString("ChatRoomsComposite_roundtable");

    public final static String LABEL_XMPP_JABBER_ID = Configuration
        .getString("text_label_xmpp_jabber_id");
    public final static String LABEL_XMPP_JABBER_SERVER = Configuration
        .getString("text_label_xmpp_jabber_server");
    public final static String LABEL_USER_NAME = Configuration
        .getString("text_label_user_name");
    public final static String LABEL_PASSWORD = Configuration
        .getString("text_label_password");
    public final static String LABEL_REPEAT_PASSWORD = Configuration
        .getString("text_label_repeat_password");

    public final static String ResourceSelectionComposite_delete_dialog_title = Configuration
        .getString("ResourceSelectionComposite_delete_dialog_title");
    public final static String ResourceSelectionComposite_overwrite_dialog_title = Configuration
        .getString("ResourceSelectionComposite_overwrite_dialog_title");

    public final static String ERROR_MESSAGE_PASSWORDS_NOT_MATCH = Configuration
        .getString("CreateXMPPAccountWizardPage_error_password_no_match");
    public final static String ERROR_MESSAGE_COULD_NOT_CONNECT = Configuration
        .getString("error_message_could_not_connect");
    public final static String ERROR_MESSAGE_NOT_CONNECTED_TO_SERVER = Configuration
        .getString("error_message_not_connected_to_server");
    public final static String ERROR_MESSAGE_ACCOUNT_ALREADY_EXISTS = Configuration
        .getString("error_message_account_already_exists");

    public final static String ERROR_MESSAGE_TOO_FAST_REGISTER_ACCOUNTS = Configuration
        .getString("error_message_too_fast_register_accounts");

    public final static String GROUP_EXISTING_ACCOUNT = Configuration
        .getString("group_existing_account");
    static public final String GROUP_TITLE_XMPP_JABBER_ACCOUNTS = Configuration
        .getString("group_title_xmpp_jabber_accounts_in_shell-saros-preferences");

    static public final String LABEL_ACTIVE_ACCOUNT_PREFIX = Configuration
        .getString("GeneralPreferencePage_active");

    static public final String BUTTON_ACTIVATE_ACCOUNT = Configuration
        .getString("button_text_activate_account_in_shell-saros-preferences");

    static public final String BUTTON_EDIT_ACCOUNT = Configuration
        .getString("button_edit_account");

    static public final String BUTTON_ADD_ACCOUNT = Configuration
        .getString("button_text_add_account_in_shell-saros-preferences");

    static public final String BUTTON_REMOVE_ACCOUNT = Configuration
        .getString("GeneralPreferencePage_REMOVE_BTN_TEXT");

    static public final String CHECKBOX_AUTO_CONNECT_ON_STARTUP = Configuration
        .getString("checkbox_label_auto_connect_on_startup_in_shell-saros-preferences");

    static public final String CHECKBOX_DISABLE_VERSION_CONTROL = Configuration
        .getString("checkbox_label_disable_version_control_support_in_shell-saros-preferences");

    static public final String CHECKBOX_ENABLE_CONCURRENT_UNDO = Configuration
        .getString("checkbox_label_enable_concurrent_undo_shell-saros-preferences");

    static public final String CHECKBOX_START_FOLLOW_MODE = Configuration
        .getString("checkbox_label_start_in_follow_mode_in_shell-saros-preferences");

    static public final String CHECKBOX_NEEDS_BASED_SYNC = Configuration
        .getString("checkbox_label_enable_needs_based_synchronisation_in_shell-saros-preferences");

    static public final String REMOVE_ACCOUNT_DIALOG_TITLE = Configuration
        .getString("GeneralPreferencePage_REMOVE_ACCOUNT_DIALOG_TITLE");

    static public final String ACTIVATE_ACCOUNT_DIALOG_TITLE = Configuration
        .getString("GeneralPreferencePage_ACTIVATE_ACCOUNT_DIALOG_TITLE");

    static public final String NODE_SAROS = Configuration
        .getString("node_saros");
    static public final String NODE_SAROS_FEEDBACK = Configuration
        .getString("node_Saros_feedback");
    static public final String NODE_SAROS_ADVANCED = Configuration
        .getString("node_Saros_Advanced");
    static public final String NODE_SAROS_NETWORK = Configuration
        .getString("node_saros_network");
    static public final String NODE_SAROS_COMMUNICATION = Configuration
        .getString("node_Saros_Communication");
    static public final String NODE_SAROS_SCREENSHARING = Configuration
        .getString("node_Saros_screensharing");
    static public final String NODE_SAROS_SCREENSHARING_DESKTOP = Configuration
        .getString("node_Saros_screensharing_desktop");
    static public final String NODE_SAROS_SCREENSHARING_ENCODER = Configuration
        .getString("node_Saros_screensharing_encoder");
    static public final String NODE_SAROS_SCREENSHARING_ENCODER_IMAGE_TILE = Configuration
        .getString("node_Saros_screensharing_encoder_image_tile");
    static public final String NODE_SAROS_SCREENSHARING_ENCODER_XUGGLER = Configuration
        .getString("node_Saros_screensharing_encoder_xuggler");
    static public final String NODE_SAROS_SCREENSHARING_REMOTE_SCREEN_VIEW = Configuration
        .getString("node_Saros_screensharing_remote_screen_view");

    public static final String PREF_NODE_SAROS_NETWORK_TRANSPORT_MODE_GROUP = Configuration
        .getString("NetworkPreferencePage_connection_established");

    public static final String PREF_NODE_SAROS_NETWORK_TRANSPORT_MODE_SOCKS5_MEDIATED_CHECKBOX = Configuration
        .getString("NetworkPreferencePage_buttonOnlyAllowMediatedSocks5_text");

    public static final String PREF_NODE_SAROS_NETWORK_TRANSPORT_MODE_IBB_CHECKBOX = Configuration
        .getString("NetworkPreferencePage_button_establish_connection");

    /* *********************************************
     * 
     * Saros View
     * 
     * ********************************************
     */

    /*
     * View infos
     */
    public final static String VIEW_SAROS = Configuration
        .getString("view_saros");
    public final static String VIEW_SAROS_ID = Configuration
        .getString("view_saros_id");
    public final static String VIEW_SAROS_WHITEBOARD = Configuration
        .getString("view_saros_whiteboard");
    public final static String VIEW_SAROS_WHITEBOARD_ID = Configuration
        .getString("view_saros_whiteboard_id");

    public final static String SHELL_REQUEST_OF_SUBSCRIPTION_RECEIVED = Configuration
        .getString("SubscriptionManager_incoming_buddy_request_title");
    public final static String SHELL_BUDDY_ALREADY_ADDED = Configuration
        .getString("shell_buddy_already_added");
    public final static String SHELL_BUDDY_LOOKUP_FAILED = Configuration
        .getString("shell_buddy_look_up_failed");
    public final static String SHELL_SERVER_NOT_FOUND = Configuration
        .getString("shell_server_not_found");
    public final static String SHELL_SET_NEW_NICKNAME = Configuration
        .getString("RenameContactAction_new_nickname_dialog_title");
    public final static String SHELL_CONFIRM_CLOSING_SESSION = Configuration
        .getString("shell_confirm_closing_session");
    public final static String SHELL_INCOMING_SCREENSHARING_SESSION = Configuration
        .getString("shell_incoming_screensharing_session");
    public final static String SHELL_SCREENSHARING_ERROR_OCCURED = Configuration
        .getString("shell_screensharing_an_error_occured");
    public final static String SHELL_INVITATION = Configuration
        .getString("shell_invitation");
    public final static String SHELL_ADD_BUDDY = Configuration
        .getString("shell_add_buddy");
    public final static String CM_ADD_BUDDY_OFFLINE = Configuration
        .getString("cm_add_buddy_offline");
    public final static String CM_ADD_BUDDY = Configuration
        .getString("cm_add_buddy");
    public final static String SHELL_ADD_BUDDY_TO_SESSION = Configuration
        .getString("shell_add_buddy_to_session");
    public final static String SHELL_ERROR_IN_SAROS_PLUGIN = Configuration
        .getString("shell_error_in_saros_plugin");
    public final static String SHELL_CLOSING_THE_SESSION = Configuration
        .getString("close_the_session");
    public final static String SHELL_CONFIRM_LEAVING_SESSION = Configuration
        .getString("confirm_leaving_session");

    public final static String SHELL_CONFIRM_DECLINE_INVITATION = Configuration
        .getString("shell_confirm_decline_invitation");

    public final static String TB_DISCONNECT = Configuration
        .getString("tb_disconnect");
    public final static String TB_ADD_A_NEW_BUDDY = Configuration
        .getString("tb_add_a_new_buddy");
    public final static String TB_CONNECT = Configuration
        .getString("tb_connect");
    public final static String TB_SHARE_SCREEN_WITH_BUDDY = Configuration
        .getString("tb_share_screen_with_buddy");
    public final static String TB_STOP_SESSION_WITH_BUDDY = Configuration
        .getString("tb_stop_session_with_user");
    public final static String TB_SEND_A_FILE_TO_SELECTED_BUDDY = Configuration
        .getString("tb_send_a_file_to_selected_buddy");
    public final static String TB_START_VOIP_SESSION = Configuration
        .getString("tb_start_a_voip_session");
    public final static String TB_NO_INCONSISTENCIES = Configuration
        .getString("tb_no_inconsistencies");
    public final static String TB_INCONSISTENCY_DETECTED = Configuration
        .getString("tb_inconsistency_detected_in");
    public final static String TB_ADD_BUDDY_TO_SESSION = Configuration
        .getString("tb_add_buddy_to_session");
    public final static String TB_RESTRICT_INVITEES_TO_READ_ONLY_ACCESS = Configuration
        .getString("tb_restrict_invitees_to_read_only_access");
    public final static String TB_ENABLE_DISABLE_FOLLOW_MODE = Configuration
        .getString("tb_enable_disable_follow_mode");
    public final static String TB_LEAVE_SESSION = Configuration
        .getString("LeaveSessionAction_leave_session_tooltip");
    public final static String TB_STOP_SESSION = Configuration
        .getString("LeaveSessionAction_stop_session_tooltip");

    public final static String CM_DELETE = Configuration.getString("cm_delete");
    public final static String CM_RENAME = Configuration.getString("cm_rename");

    public final static String CM_OPEN_CHAT = Configuration
        .getString("OpenChatAction_MenuItem");

    public final static String CM_SKYPE_THIS_BUDDY = Configuration
        .getString("cm_skype_this_buddy");
    public final static String CM_ADD_TO_SAROS_SESSION = Configuration
        .getString("cm_add_to_saros_session");
    public final static String CM_TEST_DATA_TRANSFER = Configuration
        .getString("ConnectionTestAction_title");
    public final static String CM_GRANT_WRITE_ACCESS = Configuration
        .getString("cm_grant_write_access");
    public final static String CM_RESTRICT_TO_READ_ONLY_ACCESS = Configuration
        .getString("cm_restrict_to_read_only_access");
    public final static String CM_FOLLOW_PARTICIPANT = Configuration
        .getString("cm_follow_this_buddy");
    public final static String CM_STOP_FOLLOWING = Configuration
        .getString("cm_stop_following_this_buddy");
    public final static String CM_JUMP_TO_POSITION_SELECTED_BUDDY = Configuration
        .getString("cm_jump_to_position_of_selected_buddy");
    public final static String CM_CHANGE_COLOR = Configuration
        .getString("cm_change_color");
    public final static String CM_STOP_SAROS_SESSION = Configuration
        .getString("cm_stop_saros_session");

    public final static String NODE_BUDDIES = Configuration
        .getString("tree_item_label_buddies");
    public final static String NODE_SESSION = Configuration
        .getString("tree_item_label_session");
    public final static String HOST_INDICATION = Configuration
        .getString("UserElement_host");
    public final static String NODE_NO_SESSION_RUNNING = Configuration
        .getString("tree_item_label_no_session_running");

    public final static String LABEL_XMPP_JABBER_JID = Configuration
        .getString("text_label_xmpp_jabber_jid");

    public final static String GROUP_TITLE_CREATE_NEW_XMPP_JABBER_ACCOUNT = Configuration
        .getString("group_title_create_new_xmpp_jabber_account");

    // Permission: Write Access
    public final static String READ_ONLY_ACCESS = Configuration
        .getString("read_only_access");

    public static final String FOLLOW_MODE_ENABLED = Configuration
        .getString("follow_mode_enabled");

    public static final String FOLLOW_MODE_PAUSED = Configuration
        .getString("follow_mode_paused");

    /**********************************************
     * 
     * View Remote Screen
     * 
     **********************************************/
    // View infos
    public final static String VIEW_REMOTE_SCREEN = Configuration
        .getString("view_remote_screen");
    public final static String VIEW_REMOTE_SCREEN_ID = Configuration
        .getString("view_remote_screen_id");

    public final static String TB_CHANGE_MODE_IMAGE_SOURCE = Configuration
        .getString("tb_change_mode_of_image_source");
    public final static String TB_STOP_RUNNING_SESSION = Configuration
        .getString("tb_stop_running_session");
    public final static String TB_RESUME = Configuration.getString("tb_resume");
    public final static String TB_PAUSE = Configuration.getString("tb_pause");

    /**********************************************
     * 
     * Context Menu Saros
     * 
     **********************************************/

    public final static int CREATE_NEW_PROJECT = 1;
    public final static int USE_EXISTING_PROJECT = 2;
    public final static int USE_EXISTING_PROJECT_WITH_CANCEL_LOCAL_CHANGE = 3;
    public final static int USE_EXISTING_PROJECT_WITH_COPY = 4;

    /*
     * title of shells which are pop up by performing the actions on the package
     * explorer view.
     */
    public final static String SHELL_INVITATION_CANCELLED = Configuration
        .getString("shell_invitation_cancelled");
    public final static String SHELL_SESSION_INVITATION = Configuration
        .getString("shell_session_invitation");
    public final static String SHELL_ADD_PROJECTS = Configuration
        .getString("shell_add_projects");
    public final static String SHELL_ADD_PROJECT = Configuration
        .getString("shell_add_project");
    public final static String SHELL_ADD_PROJECTS_TO_SESSION = Configuration
        .getString("shell_add_projects_to_session");
    public final static String SHELL_PROBLEM_OCCURRED = Configuration
        .getString("shell_problem_occurred");
    public final static String SHELL_WARNING_LOCAL_CHANGES_DELETED = Configuration
        .getString("shell_warning_local_changes_deleted");
    public final static String SHELL_FOLDER_SELECTION = Configuration
        .getString("shell_folder_selection");
    public final static String SHELL_SAVE_ALL_FILES_NOW = Configuration
        .getString("shell_save_all_files_now");
    public final static String SHELL_NEW_FILE_SHARED = Configuration
        .getString("shell_new_file_shared");
    public final static String SHELL_NEED_BASED_SYNC = Configuration
        .getString("shell_need_based_sync");

    public final static String SHELL_CONFIRM_SAVE_UNCHANGED_CHANGES = Configuration
        .getString("AddProjectToSessionWizard_unsaved_changes_dialog_title");

    /* Context menu of a selected tree item on the package explorer view */
    public final static String CM_SHARE_WITH = Configuration
        .getString("cm_share_with");

    public final static String CM_WORK_TOGETHER_ON = Configuration
        .getString("cm_work_together_on");
    public final static String CM_MULTIPLE_BUDDIES = Configuration
        .getString("cm_multiple_buddies");

    public final static String CM_MULTIPLE_PROJECTS = Configuration
        .getString("cm_multiple_projects");

    // public final static String CM_SHARE_PROJECT = SarosMessages
    // .getString("cm_share_project");
    public final static String CM_ADD_TO_SESSION = Configuration
        .getString("cm_add_to_session");

    /*
     * second page of the wizard "Session invitation"
     */
    public final static String RADIO_USING_EXISTING_PROJECT = Configuration
        .getString("radio_use_existing_project");
    public final static String RADIO_CREATE_NEW_PROJECT = Configuration
        .getString("radio_create_new_project");

    /**********************************************
     * 
     * ContextMenu: Open/Open With
     * 
     **********************************************/
    /* Context menu of a selected file on the package explorer view */
    public final static String CM_OPEN = "Open";
    public final static String CM_OPEN_WITH = "Open With";
    public final static String CM_OTHER = "Other...";
    public final static String CM_OPEN_WITH_TEXT_EDITOR = "Text Editor";
    public final static String CM_OPEN_WITH_SYSTEM_EDITOR = "System Editor";

    /**********************************************
     * 
     * STFBotEditor
     * 
     **********************************************/

    public final static String ID_JAVA_EDITOR = "org.eclipse.jdt.ui.CompilationUnitEditor";
    public final static String ID_TEXT_EDITOR = "org.eclipse.ui.texteditor";

    /* Title of shells */
    public static String SHELL_SAVE_RESOURCE = "Save Resource";

    /**********************************************
     * 
     * View Package Explorer
     * 
     **********************************************/

    public final static String VIEW_PACKAGE_EXPLORER = "Package Explorer";
    public final static String VIEW_PACKAGE_EXPLORER_ID = "org.eclipse.jdt.ui.PackageExplorer";

    public final static String SHELL_EDITOR_SELECTION = "Editor Selection";

    public final static String TB_COLLAPSE_ALL = "Collapse All.*";

    /**********************************************
     * 
     * View Progress
     * 
     **********************************************/
    public final static String VIEW_PROGRESS = "Progress";
    public final static String VIEW_PROGRESS_ID = "org.eclipse.ui.views.ProgressView";

    public final static String TB_REMOVE_ALL_FINISHED_OPERATIONS = "Remove All Finished Operations";

    /**********************************************
     * 
     * View Console
     * 
     **********************************************/
    public final static String VIEW_CONSOLE = "Console";
    public final static String VIEW_CONSOLE_ID = "org.eclipse.ui.console.ConsoleView";

    /**********************************************
     * 
     * View SVN Respositories
     * 
     **********************************************/
    public final static String VIEW_SVN_REPOSITORIES_ID = "org.tigris.subversion.subclipse.ui.repository.RepositoriesView";
    public final static String VIEW_SVN_REPOSITORIES = "SVN Repositories";

    /**********************************************
     * 
     * Context Menu Team
     * 
     **********************************************/
    public final static String SHELL_REVERT = "Revert";
    public final static String SHELL_SHARE_PROJECT = "Share Project";
    public final static String SHELL_SAROS_RUNNING_VCS_OPERATION = "Saros running VCS operation";
    public final static String SHELL_CONFIRM_DISCONNECT_FROM_SVN = "Confirm Disconnect from SVN";
    static public final String SHELL_IMPORT = "Import";
    static public final String SHELL_SWITCH = "Switch";
    static public final String SHELL_SVN_SWITCH = "SVN Switch";

    public final static String LABEL_CREATE_A_NEW_REPOSITORY_LOCATION = "Create a new repository location";
    public final static String LABEL_URL = "Url:";
    public final static String LABEL_TO_URL = "To URL:";
    static public final String LABEL_SWITCH_TOHEAD_REVISION = "Switch to HEAD revision";
    static public final String LABEL_REVISION = "Revision:";

    /* All the sub menus of the context menu "Team" */
    public final static String CM_REVERT = "Revert...";
    public final static String CM_DISCONNECT = "Disconnect...";
    public final static String CM_SHARE_PROJECT_OF_TEAM = "Share Project...";
    public final static String CM_SWITCH_TO_ANOTHER_BRANCH_TAG_REVISION = "Switch to another Branch/Tag/Revision...";
    public final static String CM_TEAM = "Team";

    /* table iems of the shell "Share project" of the conext menu "Team" */
    public final static String TABLE_ITEM_REPOSITORY_TYPE_SVN = "SVN";

    /*
     * Invit. / Synch. Monitor Names
     */

    public static final String SHELL_MONITOR_PROJECT_SYNCHRONIZATION = Configuration
        .getString("shell_monitor_project_synchronization");
    /**********************************************
     * 
     * Others
     * 
     **********************************************/
    public final static String STRING_REGEX_WITH_LINE_BREAK = ".*\n*.*";
    public final static String PKG_REGEX = "[\\w*\\.]*\\w*";
    public final static String PROJECT_REGEX = "\\w*";

    /* Saros Preferences -> Advanced */

    public static final String SAROS_ADVANCED_GROUP_FILE_TRANSFER = Configuration
        .getString("saros_advanced_group_file_transfer");

    public static final String SAROS_ADVANCED_GROUP_FILE_TRANSFER_FORCE_IBB = Configuration
        .getString("saros_advanced_group_file_transfer_force_ibb");

}
