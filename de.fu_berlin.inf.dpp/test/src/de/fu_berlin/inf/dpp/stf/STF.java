package de.fu_berlin.inf.dpp.stf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.swtbot.swt.finder.utils.FileUtils;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.osgi.framework.Bundle;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.feedback.FeedbackManager;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;

public class STF {

    protected static final transient Logger log = Logger.getLogger(STF.class);

    public enum TreeItemType {
        JAVA_PROJECT, PROJECT, FILE, CLASS, PKG, FOLDER, NULL
    }

    public enum BuddyRole {
        HOST, PEER
    }

    public static SarosSessionManager sessionManager;
    public static DataTransferManager dataTransferManager;
    public static EditorManager editorManager;
    public static XMPPAccountStore xmppAccountStore;
    public static FeedbackManager feedbackManager;
    public static Saros saros;

    // local JID
    public static JID localJID;

    protected final static String CONFIRM_DELETE = "Confirm Delete";
    protected final static String SHELL_COPY_PROJECT = "Copy Project";
    protected final static String SHELL_DELETING_ACTIVE_ACCOUNT = "Deleting active account";

    /**********************************************
     * 
     * Basic Widgets
     * 
     **********************************************/
    // Title of Buttons
    protected final static String YES = "Yes";
    protected final static String OK = "OK";
    protected final static String NO = "No";
    protected final static String CANCEL = "Cancel";
    protected final static String FINISH = "Finish";
    protected final static String APPLY = "Apply";
    protected final static String NEXT = "Next >";
    protected final static String BROWSE = "Browse";

    protected final static String SRC = "src";
    protected final static String SUFFIX_JAVA = ".java";

    /**********************************************
     * 
     * View Progress
     * 
     **********************************************/
    protected final static String SHELL_PROGRESS_INFORMATION = "Progress Information";

    /**********************************************
     * 
     * Dialog Preferences
     * 
     **********************************************/
    static protected final String NODE_CONSOLE = "Console";
    static protected final String NODE_EDITORS = "Editors";
    static protected final String NODE_TEXT_EDITORS = "Text Editors";
    static protected final String NODE_ANNOTATIONS = "Annotations";
    /**********************************************
     * 
     * Main Menu File
     * 
     **********************************************/

    static protected final String MENU_NEW = "New";
    static protected final String MENU_PROJECT = "Project...";
    static protected final String MENU_FOLDER = "Folder";
    static protected final String MENU_FILE = "File";
    static protected final String MENU_CLASS = "Class";
    static protected final String MENU_PACKAGE = "Package";
    static protected final String MENU_JAVA_PROJECT = "Java Project";
    static protected final String MENU_CLOSE = "Close";
    static protected final String MENU_CLOSE_ALL = "Close All";

    static protected final String MENU_SAVE = "Save";
    static protected final String MENU_SAVE_AS = "Save As...";
    static protected final String MENU_SAVE_All = "Save All";

    protected final static String SHELL_NEW_FOLDER = "New Folder";
    protected final static String SHELL_NEW_FILE = "New File";
    protected final static String SHELL_NEW_JAVA_PACKAGE = "New Java Package";
    protected final static String SHELL_NEW_JAVA_CLASS = "New Java Class";
    static protected final String SHELL_NEW_PROJECT = "New Project";
    static protected final String SHELL_NEW_JAVA_PROJECT = "New Java Project";

    /* categories and nodes of the shell "New Project" */
    static protected final String NODE_GENERAL = "General";
    static protected final String NODE_PROJECT = "Project";

    static protected final String LABEL_PROJECT_NAME = "Project name:";
    static protected final String LABEL_FILE_NAME = "File name:";
    static protected final String LABEL_FOLDER_NAME = "Folder name:";
    static protected final String LABEL_ENTER_OR_SELECT_THE_PARENT_FOLDER = "Enter or select the parent folder:";
    static protected final String LABEL_SOURCE_FOLDER = "Source folder:";
    static protected final String LABEL_PACKAGE = "Package:";
    static protected final String LABEL_NAME = "Name:";

    /**********************************************
     * 
     * Main Menu Edit
     * 
     **********************************************/
    protected final static String SHELL_DELETE_RESOURCE = "Delete Resources";

    /* menu names */
    protected final static String MENU_DELETE = "Delete";
    protected final static String MENU_EDIT = "Edit";
    protected final static String MENU_COPY = "Copy";
    protected final static String MENU_PASTE = "Paste";

    /**********************************************
     * 
     * Main Menu Refactor
     * 
     **********************************************/
    /* shell titles */
    protected final static String SHELL_MOVE = "Move";
    protected final static String SHELL_RENAME_PACKAGE = "Rename Package";
    protected final static String SHELL_RENAME_RESOURCE = "Rename Resource";
    protected final static String SHELL_RENAME_COMPiIATION_UNIT = "Rename Compilation Unit";
    protected final static String LABEL_NEW_NAME = "New name:";

    /* menu names */
    protected final static String MENU_REFACTOR = "Refactor";
    protected final static String MENU_RENAME = "Rename...";
    protected final static String MENU_MOVE = "Move...";

    /**********************************************
     * 
     * Main Menu Window
     * 
     **********************************************/

    static protected final String TREE_ITEM_GENERAL_IN_PRFERENCES = "General";
    static protected final String TREE_ITEM_WORKSPACE_IN_PREFERENCES = "Workspace";
    static protected final String TREE_ITEM_GENERAL_IN_SHELL_SHOW_VIEW = "General";
    static protected final String TREE_ITEM_PROBLEM_IN_SHELL_SHOW_VIEW = "Problems";
    static protected final String TREE_ITEM_PROJECT_EXPLORER_IN_SHELL_SHOW_VIEW = "Project Explorer";

    /* name of all the main menus */
    static protected final String MENU_WINDOW = "Window";
    protected final static String MENU_OTHER = "Other...";
    protected final static String MENU_SHOW_VIEW = "Show View";

    static protected final String SHELL_SHOW_VIEW = "Show View";

    /* IDs of all the perspectives */
    protected final static String ID_JAVA_PERSPECTIVE = "org.eclipse.jdt.ui.JavaPerspective";
    protected final static String ID_DEBUG_PERSPECTIVE = "org.eclipse.debug.ui.DebugPerspective";
    protected final static String ID_RESOURCE_PERSPECTIVE = "eclipse.ui.resourcePerspective";

    /**********************************************
     * 
     * Main Menu Saros
     * 
     **********************************************/

    protected final static String MENU_SAROS = SarosMessages
        .getString("menu_saros");
    protected final static String MENU_START_SAROS_CONFIGURATION = SarosMessages
        .getString("menu_start_saros_configuration");
    protected final static String MENU_CREATE_ACCOUNT = SarosMessages
        .getString("menu_create_account");
    protected final static String MENU_ADD_BUDDY = SarosMessages
        .getString("menu_add_buddy");
    protected final static String MENU_SHARE_PROJECTS = SarosMessages
        .getString("menu_share_projects");
    protected final static String MENU_PREFERENCES = SarosMessages
        .getString("menu_preferences");

    protected final static String SHELL_PREFERNCES = SarosMessages
        .getString("shell_preferences");
    protected final static String SHELL_CREATE_NEW_XMPP_ACCOUNT = SarosMessages
        .getString("shell_create_new_xmpp_account");
    protected final static String SHELL_SAROS_CONFIGURATION = SarosMessages
        .getString("shell_saros_configuration");

    protected final static String SHELL_ADD_XMPP_JABBER_ACCOUNT = SarosMessages
        .getString("shell_add_xmpp_jabber_account");

    protected final static String SHELL_CHANGE_ACCOUNT = SarosMessages
        .getString("shell_change_xmpp_account");

    protected final static String LABEL_XMPP_JABBER_ID = SarosMessages
        .getString("text_label_xmpp_jabber_id");
    protected final static String LABEL_XMPP_JABBER_SERVER = SarosMessages
        .getString("text_label_xmpp_jabber_server");
    protected final static String LABEL_USER_NAME = SarosMessages
        .getString("text_label_user_name");
    protected final static String LABEL_PASSWORD = SarosMessages
        .getString("text_label_password");
    protected final static String LABEL_REPEAT_PASSWORD = SarosMessages
        .getString("text_label_repeat_password");

    protected final static String ERROR_MESSAGE_PASSWORDS_NOT_MATCH = SarosMessages
        .getString("error_message_passwords_not_match");
    protected final static String ERROR_MESSAGE_COULD_NOT_CONNECT = SarosMessages
        .getString("error_message_could_not_connect");
    protected final static String ERROR_MESSAGE_NOT_CONNECTED_TO_SERVER = SarosMessages
        .getString("error_message_not_connected_to_server");
    protected final static String ERROR_MESSAGE_ACCOUNT_ALREADY_EXISTS = SarosMessages
        .getString("error_message_account_already_exists");

    protected final static String ERROR_MESSAGE_TOO_FAST_REGISTER_ACCOUNTS = SarosMessages
        .getString("error_message_too_fast_register_accounts");

    protected final static String GROUP_EXISTING_ACCOUNT = SarosMessages
        .getString("group_existing_account");
    static protected final String GROUP_TITLE_XMPP_JABBER_ACCOUNTS = SarosMessages
        .getString("group_title_xmpp_jabber_accounts_in_shell-saros-preferences");

    static protected final String BUTTON_ACTIVATE_ACCOUNT = SarosMessages
        .getString("button_text_activate_account_in_shell-saros-preferences");

    static protected final String BUTTON_CHANGE_ACCOUNT = SarosMessages
        .getString("button_text_change_account_in_shell-saros-preferences");
    static protected final String BUTTON_ADD_ACCOUNT = SarosMessages
        .getString("button_text_add_account_in_shell-saros-preferences");
    static protected final String BUTTON_DELETE_ACCOUNT = SarosMessages
        .getString("button_text_delete_account_in_shell-saros-preferences");

    static protected final String CHECKBOX_AUTO_CONNECT_ON_STARTUP = SarosMessages
        .getString("checkbox_label_auto_connect_on_startup_in_shell-saros-preferences");

    static protected final String CHECKBOX_DISABLE_VERSION_CONTROL = SarosMessages
        .getString("checkbox_label_disable_version_control_support_in_shell-saros-preferences");

    static protected final String CHECKBOX_ENABLE_CONCURRENT_UNDO = SarosMessages
        .getString("checkbox_label_enable_concurrent_undo_shell-saros-preferences");

    static protected final String CHECKBOX_START_FOLLOW_MODE = SarosMessages
        .getString("checkbox_label_start_in_follow_mode_in_shell-saros-preferences");

    static protected final String NODE_SAROS = SarosMessages
        .getString("node_saros");
    static protected final String NODE_SAROS_FEEDBACK = SarosMessages
        .getString("node_Saros_feedback");
    static protected final String NODE_SAROS_ADVANCED = SarosMessages
        .getString("node_Saros_Advanced");
    static protected final String NODE_SAROS_COMMUNICATION = SarosMessages
        .getString("node_Saros_Communication");
    static protected final String NODE_SAROS_SCREENSHARING = SarosMessages
        .getString("node_Saros_screensharing");
    static protected final String NODE_SAROS_SCREENSHARING_DESKTOP = SarosMessages
        .getString("node_Saros_screensharing_desktop");
    static protected final String NODE_SAROS_SCREENSHARING_ENCODER = SarosMessages
        .getString("node_Saros_screensharing_encoder");
    static protected final String NODE_SAROS_SCREENSHARING_ENCODER_IMAGE_TILE = SarosMessages
        .getString("node_Saros_screensharing_encoder_image_tile");
    static protected final String NODE_SAROS_SCREENSHARING_ENCODER_XUGGLER = SarosMessages
        .getString("node_Saros_screensharing_encoder_xuggler");
    static protected final String NODE_SAROS_SCREENSHARING_REMOTE_SCREEN_VIEW = SarosMessages
        .getString("node_Saros_screensharing_remote_screen_view");

    /**********************************************
     * 
     * View Saros Buddies
     * 
     **********************************************/
    /* View infos */
    protected final static String VIEW_SAROS_BUDDIES = SarosMessages
        .getString("view_saros_buddies");
    protected final static String VIEW_SAROS_BUDDIES_ID = SarosMessages
        .getString("view_saros_buddies_id");

    protected final static String SHELL_REQUEST_OF_SUBSCRIPTION_RECEIVED = SarosMessages
        .getString("shell_request_of_subscription_received");
    protected final static String SHELL_BUDDY_ALREADY_ADDED = SarosMessages
        .getString("shell_buddy_already_added");
    protected final static String SHELL_ADD_BUDDY = SarosMessages
        .getString("shell_add_buddy");
    protected final static String SHELL_BUDDY_LOOKUP_FAILED = SarosMessages
        .getString("shell_buddy_look_up_failed");
    protected final static String SHELL_SERVER_NOT_FOUND = SarosMessages
        .getString("shell_server_not_found");
    protected final static String SHELL_REMOVAL_OF_SUBSCRIPTION = SarosMessages
        .getString("shell_removal_of_subscription");

    protected final static String SHELL_SET_NEW_NICKNAME = SarosMessages
        .getString("shell_set_new_nickname");

    protected final static String TB_DISCONNECT = SarosMessages
        .getString("tb_disconnect");
    protected final static String TB_ADD_A_NEW_BUDDY = SarosMessages
        .getString("tb_add_a_new_buddy");
    protected final static String TB_CONNECT = SarosMessages
        .getString("tb_connect");

    protected final static String CM_DELETE = SarosMessages
        .getString("cm_delete");
    protected final static String CM_RENAME = SarosMessages
        .getString("cm_rename");
    protected final static String CM_SKYPE_THIS_BUDDY = SarosMessages
        .getString("cm_skype_this_buddy");
    protected final static String CM_INVITE_BUDDY = SarosMessages
        .getString("cm_invite_buddy");
    protected final static String CM_TEST_DATA_TRANSFER = SarosMessages
        .getString("cm_test_data_transfer_connection");

    protected final static String NODE_BUDDIES = SarosMessages
        .getString("tree_item_label_buddies");

    protected final static String LABEL_XMPP_JABBER_JID = SarosMessages
        .getString("text_label_xmpp_jabber_jid");

    protected final static String GROUP_TITLE_CREATE_NEW_XMPP_JABBER_ACCOUNT = SarosMessages
        .getString("group_title_create_new_xmpp_jabber_account");

    /**********************************************
     * 
     * View Saros Session
     * 
     **********************************************/
    /*
     * View infos
     */
    protected final static String VIEW_SAROS_SESSION = SarosMessages
        .getString("view_saros_session");
    protected final static String VIEW_SAROS_SESSION_ID = SarosMessages
        .getString("view_saros_session_id");

    // Permission: Write Access
    static protected final String OWN_PARTICIPANT_NAME = SarosMessages
        .getString("own_participant_name");
    protected final static String PERMISSION_NAME = SarosMessages
        .getString("permission_name");

    /*
     * title of shells which are pop up by performing the actions on the view.
     */
    protected final static String SHELL_CONFIRM_CLOSING_SESSION = SarosMessages
        .getString("shell_confirm_closing_session");
    protected final static String SHELL_INCOMING_SCREENSHARING_SESSION = SarosMessages
        .getString("shell_incoming_screensharing_session");
    protected final static String SHELL_SCREENSHARING_ERROR_OCCURED = SarosMessages
        .getString("shell_screensharing_an_error_occured");
    protected final static String SHELL_INVITATION = SarosMessages
        .getString("shell_invitation");
    protected final static String SHELL_ADD_BUDDY_TO_SESSION = SarosMessages
        .getString("shell_add_buddy_to_session");

    protected final static String SHELL_ERROR_IN_SAROS_PLUGIN = SarosMessages
        .getString("shell_error_in_saros_plugin");
    protected final static String SHELL_CLOSING_THE_SESSION = SarosMessages
        .getString("close_the_session");
    protected final static String SHELL_CONFIRM_LEAVING_SESSION = SarosMessages
        .getString("comfirm_leaving_session");

    /*
     * Tool tip text of all the toolbar buttons on the view
     */
    protected final static String TB_SHARE_SCREEN_WITH_BUDDY = SarosMessages
        .getString("tb_share_screen_with_buddy");
    protected final static String TB_STOP_SESSION_WITH_BUDDY = SarosMessages
        .getString("tb_stop_session_with_user");
    protected final static String TB_SEND_A_FILE_TO_SELECTED_BUDDY = SarosMessages
        .getString("tb_send_a_file_to_selected_buddy");
    protected final static String TB_START_VOIP_SESSION = SarosMessages
        .getString("tb_start_a_voip_session");
    protected final static String TB_NO_INCONSISTENCIES = SarosMessages
        .getString("tb_no_inconsistencies");
    protected final static String TB_INCONSISTENCY_DETECTED = SarosMessages
        .getString("tb_inconsistency_detected_in");
    protected final static String TB_ADD_BUDDY_TO_SESSION = SarosMessages
        .getString("tb_add_buddy_to_session");
    protected final static String TB_RESTRICT_INVITEES_TO_READ_ONLY_ACCESS = SarosMessages
        .getString("tb_restrict_invitees_to_read_only_access");
    protected final static String TB_ENABLE_DISABLE_FOLLOW_MODE = SarosMessages
        .getString("tb_enable_disable_follow_mode");
    protected final static String TB_LEAVE_SESSION = SarosMessages
        .getString("tb_leave_session");
    protected final static String TB_STOP_SESSION = SarosMessages
        .getString("tb_stop_session");

    // Context menu's name of the table on the view
    protected final static String CM_GRANT_WRITE_ACCESS = SarosMessages
        .getString("cm_grant_write_access");
    protected final static String CM_RESTRICT_TO_READ_ONLY_ACCESS = SarosMessages
        .getString("cm_restrict_to_read_only_access");
    protected final static String CM_FOLLOW_THIS_BUDDY = SarosMessages
        .getString("cm_follow_this_buddy");
    protected final static String CM_STOP_FOLLOWING_THIS_BUDDY = SarosMessages
        .getString("cm_stop_following_this_buddy");
    protected final static String CM_JUMP_TO_POSITION_SELECTED_BUDDY = SarosMessages
        .getString("cm_jump_to_position_of_selected_buddy");
    protected final static String CM_CHANGE_COLOR = SarosMessages
        .getString("cm_change_color");

    /**********************************************
     * 
     * View Saros Chat
     * 
     **********************************************/
    protected final static String VIEW_SAROS_CHAT = SarosMessages
        .getString("view_saros_chat");
    protected final static String VIEW_SAROS_CHAT_ID = SarosMessages
        .getString("view_saros_chat_id");

    /**********************************************
     * 
     * View Remote Screen
     * 
     **********************************************/
    // View infos
    protected final static String VIEW_REMOTE_SCREEN = SarosMessages
        .getString("view_remote_screen");
    protected final static String VIEW_REMOTE_SCREEN_ID = SarosMessages
        .getString("view_remote_screen_id");

    protected final static String TB_CHANGE_MODE_IMAGE_SOURCE = SarosMessages
        .getString("tb_change_mode_of_image_source");
    protected final static String TB_STOP_RUNNING_SESSION = SarosMessages
        .getString("tb_stop_running_session");
    protected final static String TB_RESUME = SarosMessages
        .getString("tb_resume");
    protected final static String TB_PAUSE = SarosMessages
        .getString("tb_pause");

    /**********************************************
     * 
     * Context Menu Saros
     * 
     **********************************************/

    protected final static int CREATE_NEW_PROJECT = 1;
    protected final static int USE_EXISTING_PROJECT = 2;
    protected final static int USE_EXISTING_PROJECT_WITH_CANCEL_LOCAL_CHANGE = 3;
    protected final static int USE_EXISTING_PROJECT_WITH_COPY = 4;

    /*
     * title of shells which are pop up by performing the actions on the package
     * explorer view.
     */
    protected final static String SHELL_INVITATION_CANCELLED = SarosMessages
        .getString("shell_invitation_cancelled");
    protected final static String SHELL_SESSION_INVITATION = SarosMessages
        .getString("shell_session_invitation");
    protected final static String SHELL_ADD_PROJECT = SarosMessages
        .getString("shell_add_project");
    protected final static String SHELL_PROBLEM_OCCURRED = SarosMessages
        .getString("shell_problem_occurred");
    protected final static String SHELL_WARNING_LOCAL_CHANGES_DELETED = SarosMessages
        .getString("shell_warning_local_changes_deleted");
    protected final static String SHELL_FOLDER_SELECTION = SarosMessages
        .getString("shell_folder_selection");
    protected final static String SHELL_SAVE_ALL_FILES_NOW = SarosMessages
        .getString("shell_save_all_files_now");

    /* Context menu of a selected tree item on the package explorer view */
    protected final static String CM_SHARE_WITH = SarosMessages
        .getString("cm_share_with");
    protected final static String CM_MULTIPLE_BUDDIES = SarosMessages
        .getString("cm_multiple_buddies");
    protected final static String CM_SHARE_PROJECT = SarosMessages
        .getString("cm_share_project");
    protected final static String CM_ADD_TO_SESSION = SarosMessages
        .getString("cm_add_to_session");

    /*
     * second page of the wizard "Session invitation"
     */
    protected final static String RADIO_USING_EXISTING_PROJECT = SarosMessages
        .getString("radio_use_existing_project");
    protected final static String RADIO_CREATE_NEW_PROJECT = SarosMessages
        .getString("radio_create_new_project");

    /**********************************************
     * 
     * ContextMenu: Open/Open With
     * 
     **********************************************/
    /* Context menu of a selected file on the package explorer view */
    protected final static String CM_OPEN = "Open";
    protected final static String CM_OPEN_WITH = "Open With";
    protected final static String CM_OTHER = "Other...";
    protected final static String CM_OPEN_WITH_TEXT_EDITOR = "Text Editor";
    protected final static String CM_OPEN_WITH_SYSTEM_EDITOR = "System Editor";

    /**********************************************
     * 
     * STFBotEditor
     * 
     **********************************************/

    protected final static String ID_JAVA_EDITOR = "org.eclipse.jdt.ui.CompilationUnitEditor";
    protected final static String ID_TEXT_EDITOR = "org.eclipse.ui.texteditor";

    /* Title of shells */
    protected static String SHELL_SAVE_RESOURCE = "Save Resource";

    /**********************************************
     * 
     * View Package Explorer
     * 
     **********************************************/

    protected final static String VIEW_PACKAGE_EXPLORER = "Package Explorer";
    protected final static String VIEW_PACKAGE_EXPLORER_ID = "org.eclipse.jdt.ui.PackageExplorer";

    protected final static String SHELL_EDITOR_SELECTION = "Editor Selection";

    protected final static String TB_COLLAPSE_ALL = "Collapse All.*";

    /**********************************************
     * 
     * View Progress
     * 
     **********************************************/
    protected final static String VIEW_PROGRESS = "Progress";
    protected final static String VIEW_PROGRESS_ID = "org.eclipse.ui.views.ProgressView";

    protected final static String TB_REMOVE_ALL_FINISHED_OPERATIONS = "Remove All Finished Operations";

    /**********************************************
     * 
     * View Console
     * 
     **********************************************/
    protected final static String VIEW_CONSOLE = "Console";

    /**********************************************
     * 
     * View SVN Respositories
     * 
     **********************************************/
    protected final static String VIEW_SVN_REPOSITORIES_ID = "org.tigris.subversion.subclipse.ui.repository.RepositoriesView";
    protected final static String VIEW_SVN_REPOSITORIES = "SVN Repositories";

    /**********************************************
     * 
     * Context Menu Team
     * 
     **********************************************/
    protected final static String SHELL_REVERT = "Revert";
    protected final static String SHELL_SHARE_PROJECT = "Share Project";
    protected final static String SHELL_SAROS_RUNNING_VCS_OPERATION = "Saros running VCS operation";
    protected final static String SHELL_CONFIRM_DISCONNECT_FROM_SVN = "Confirm Disconnect from SVN";
    static protected final String SHELL_IMPORT = "Import";
    static protected final String SHELL_SWITCH = "Switch";
    static protected final String SHELL_SVN_SWITCH = "SVN Switch";

    protected final static String LABEL_CREATE_A_NEW_REPOSITORY_LOCATION = "Create a new repository location";
    protected final static String LABEL_URL = "Url:";
    protected final static String LABEL_TO_URL = "To URL:";
    static protected final String LABEL_SWITCH_TOHEAD_REVISION = "Switch to HEAD revision";
    static protected final String LABEL_REVISION = "Revision:";

    /* All the sub menus of the context menu "Team" */
    protected final static String CM_REVERT = "Revert...";
    protected final static String CM_DISCONNECT = "Disconnect...";
    protected final static String CM_SHARE_PROJECT_OF_TEAM = "Share Project...";
    protected final static String CM_SWITCH_TO_ANOTHER_BRANCH_TAG_REVISION = "Switch to another Branch/Tag/Revision...";
    protected final static String CM_TEAM = "Team";

    /* table iems of the shell "Share project" of the conext menu "Team" */
    protected final static String TABLE_ITEM_REPOSITORY_TYPE_SVN = "SVN";

    /**********************************************
     * 
     * Others
     * 
     **********************************************/
    protected final static String STRING_REGEX_WITH_LINE_BREAK = ".*\n*.*";
    public final static String PKG_REGEX = "[\\w*\\.]*\\w*";
    public final static String PROJECT_REGEX = "\\w*";

    public final static String SCREENSHOTDIR = "test/STF/screenshot";

    protected final static Map<String, String> viewTitlesAndIDs = new HashMap<String, String>();
    static {
        viewTitlesAndIDs.put(VIEW_PACKAGE_EXPLORER, VIEW_PACKAGE_EXPLORER_ID);
        viewTitlesAndIDs.put(VIEW_REMOTE_SCREEN, VIEW_REMOTE_SCREEN_ID);
        viewTitlesAndIDs.put(VIEW_SAROS_BUDDIES, VIEW_SAROS_BUDDIES_ID);
        viewTitlesAndIDs.put(VIEW_SAROS_CHAT, VIEW_SAROS_CHAT_ID);
        viewTitlesAndIDs.put(VIEW_SAROS_SESSION, VIEW_SAROS_SESSION_ID);
        viewTitlesAndIDs.put(VIEW_SVN_REPOSITORIES, VIEW_SVN_REPOSITORIES_ID);
        viewTitlesAndIDs.put(VIEW_PROGRESS, VIEW_PROGRESS_ID);
    }

    /**********************************************
     * 
     * Common convenient functions
     * 
     **********************************************/
    public static TypeOfOS getOS() {
        String osName = System.getProperty("os.name");
        if (osName.matches("Windows.*"))
            return TypeOfOS.WINDOW;
        else if (osName.matches("Mac OS X.*")) {
            return TypeOfOS.MAC;
        }
        return TypeOfOS.WINDOW;
    }

    public enum TypeOfOS {
        MAC, WINDOW
    }

    public String getClassPath(String projectName, String pkg, String className) {
        return projectName + "/src/" + pkg.replaceAll("\\.", "/") + "/"
            + className + ".java";
    }

    public String getPkgPath(String projectName, String pkg) {
        return projectName + "/src/" + pkg.replaceAll("\\.", "/");
    }

    public String[] getClassNodes(String projectName, String pkg,
        String className) {
        String[] nodes = { projectName, SRC, pkg, className + SUFFIX_JAVA };
        return nodes;
    }

    public String[] getPkgNodes(String projectName, String pkg) {
        String[] nodes = { projectName, SRC, pkg };
        return nodes;
    }

    public String getPath(String... nodes) {
        String folderpath = "";
        for (int i = 0; i < nodes.length; i++) {
            if (i == nodes.length - 1) {

                folderpath += nodes[i];
            } else
                folderpath += nodes[i] + "/";
        }
        return folderpath;
    }

    public String changeToRegex(String text) {
        return text + ".*";
    }

    public String[] changeToRegex(String... texts) {
        String[] matchTexts = new String[texts.length];
        for (int i = 0; i < texts.length; i++) {
            matchTexts[i] = texts[i] + "( .*)?";
        }
        return matchTexts;
    }

    public String ConvertStreamToString(InputStream is) throws IOException {
        if (is != null) {
            Writer writer = new StringWriter();
            char[] buffer = new char[5024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(is,
                    "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                is.close();
                writer.close();
            }
            return writer.toString();
        } else {
            return "";
        }
    }

    public boolean isSame(InputStream input1, InputStream input2)
        throws IOException {
        boolean error = false;
        try {
            byte[] buffer1 = new byte[1024];
            byte[] buffer2 = new byte[1024];
            try {
                int numRead1 = 0;
                int numRead2 = 0;
                while (true) {
                    numRead1 = input1.read(buffer1);
                    numRead2 = input2.read(buffer2);
                    if (numRead1 > -1) {
                        if (numRead2 != numRead1)
                            return false;
                        // Otherwise same number of bytes read
                        if (!Arrays.equals(buffer1, buffer2))
                            return false;
                        // Otherwise same bytes read, so continue ...
                    } else {
                        // Nothing more in stream 1 ...
                        return numRead2 < 0;
                    }
                }
            } finally {
                input1.close();
            }
        } catch (IOException e) {
            error = true;
            throw e;
        } catch (RuntimeException e) {
            error = true;
            throw e;
        } finally {
            try {
                input2.close();
            } catch (IOException e) {
                if (!error)
                    throw e;
            }
        }
    }

    public String checkInputText(String inputText) {
        char[] chars = inputText.toCharArray();
        String newInputText = "";

        for (char c : chars) {
            if (c == 'y' && SWTBotPreferences.KEYBOARD_LAYOUT.equals("MAC_DE")) {
                newInputText += 'z';
            } else
                newInputText += c;
        }
        return newInputText;
    }

    public boolean isValidClassPath(String projectName, String pkg,
        String className) {
        boolean isVailid = true;
        isVailid &= projectName.matches(PROJECT_REGEX);
        isVailid &= pkg.matches(PKG_REGEX);
        isVailid &= className.matches("\\w*");
        return isVailid;
    }

    public String[] getParentNodes(String... nodes) {
        String[] parentNodes = new String[nodes.length - 1];
        for (int i = 0; i < nodes.length - 1; i++) {
            parentNodes[i] = nodes[i];
        }
        return parentNodes;
    }

    public String getLastNode(String... nodes) {
        return nodes[nodes.length - 1];
    }

    public String[] splitPkg(String pkg) {
        return pkg.split(".");
    }

    public String getFileContentNoGUI(String filePath) {
        Bundle bundle = saros.getBundle();
        String content;
        try {
            content = FileUtils.read(bundle.getEntry(filePath));
        } catch (NullPointerException e) {
            throw new RuntimeException("Could not open " + filePath);
        }
        return content;
    }

}
