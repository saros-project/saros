package de.fu_berlin.inf.dpp.stf.stfMessages;

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
import de.fu_berlin.inf.dpp.net.JID;

public class STFMessages {

    public static final transient Logger log = Logger.getLogger(STFMessages.class);

    public enum TreeItemType {
        JAVA_PROJECT, PROJECT, FILE, CLASS, PKG, FOLDER, NULL
    }

    public enum BuddyRole {
        HOST, PEER
    }

    public static Saros saros;

    // local JID
    public static JID localJID;

    public final static String CONFIRM_DELETE = "Confirm Delete";
    public final static String SHELL_COPY_PROJECT = "Copy Project";
    public final static String SHELL_DELETING_ACTIVE_ACCOUNT = "Deleting active account";

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
    public final static String APPLY = "Apply";
    public final static String NEXT = "Next >";
    public final static String BROWSE = "Browse";

    public final static String SRC = "src";
    public final static String SUFFIX_JAVA = ".java";

    /**********************************************
     * 
     * View Progress
     * 
     **********************************************/
    public final static String SHELL_PROGRESS_INFORMATION = "Progress Information";

    /**********************************************
     * 
     * Dialog Preferences
     * 
     **********************************************/
    static public final String NODE_CONSOLE = "Console";
    static public final String NODE_EDITORS = "Editors";
    static public final String NODE_TEXT_EDITORS = "Text Editors";
    static public final String NODE_ANNOTATIONS = "Annotations";
    /**********************************************
     * 
     * Main Menu File
     * 
     **********************************************/

    static public final String MENU_NEW = "New";
    static public final String MENU_PROJECT = "Project...";
    static public final String MENU_FOLDER = "Folder";
    static public final String MENU_FILE = "File";
    static public final String MENU_CLASS = "Class";
    static public final String MENU_PACKAGE = "Package";
    static public final String MENU_JAVA_PROJECT = "Java Project";
    static public final String MENU_CLOSE = "Close";
    static public final String MENU_CLOSE_ALL = "Close All";

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

    static public final String LABEL_PROJECT_NAME = "Project name:";
    static public final String LABEL_FILE_NAME = "File name:";
    static public final String LABEL_FOLDER_NAME = "Folder name:";
    static public final String LABEL_ENTER_OR_SELECT_THE_PARENT_FOLDER = "Enter or select the parent folder:";
    static public final String LABEL_SOURCE_FOLDER = "Source folder:";
    static public final String LABEL_PACKAGE = "Package:";
    static public final String LABEL_NAME = "Name:";

    /**********************************************
     * 
     * Main Menu Edit
     * 
     **********************************************/
    public final static String SHELL_DELETE_RESOURCE = "Delete Resources";

    /* menu names */
    public final static String MENU_DELETE = "Delete";
    public final static String MENU_EDIT = "Edit";
    public final static String MENU_COPY = "Copy";
    public final static String MENU_PASTE = "Paste";

    /**********************************************
     * 
     * Main Menu Refactor
     * 
     **********************************************/
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

    /**********************************************
     * 
     * Main Menu Window
     * 
     **********************************************/

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

    /**********************************************
     * 
     * Multiused label name.
     * 
     **********************************************/

    public final static String ADD_PROJECTS = Messages
        .getString("add_projects");

    /**********************************************
     * 
     * Main Menu Saros
     * 
     **********************************************/

    public final static String MENU_SAROS = Messages
        .getString("menu_saros");
    public final static String MENU_START_SAROS_CONFIGURATION = Messages
        .getString("menu_start_saros_configuration");
    public final static String MENU_CREATE_ACCOUNT = Messages
        .getString("menu_create_account");
    public final static String MENU_ADD_BUDDY = Messages
        .getString("menu_add_buddy");
    public final static String ADD_BUDDIES = Messages
        .getString("add_buddies");
    public final static String SHARE_PROJECTS = Messages
        .getString("share_projects");
    public final static String MENU_PREFERENCES = Messages
        .getString("menu_preferences");
    public final static String MENU_STOP_SESSION = Messages
        .getString("menu_stop_session");

    public final static String SHELL_PREFERNCES = Messages
        .getString("shell_preferences");
    public final static String SHELL_CREATE_XMPP_JABBER_ACCOUNT = Messages
        .getString("shell_create_xmpp_jabber_account");
    public final static String SHELL_SAROS_CONFIGURATION = Messages
        .getString("shell_saros_configuration");

    public final static String SHELL_ADD_XMPP_JABBER_ACCOUNT = Messages
        .getString("shell_add_xmpp_jabber_account");

    public final static String SHELL_EDIT_XMPP_JABBER_ACCOUNT = Messages
        .getString("shell_edit_xmpp_jabber_account");

    public final static String LABEL_XMPP_JABBER_ID = Messages
        .getString("text_label_xmpp_jabber_id");
    public final static String LABEL_XMPP_JABBER_SERVER = Messages
        .getString("text_label_xmpp_jabber_server");
    public final static String LABEL_USER_NAME = Messages
        .getString("text_label_user_name");
    public final static String LABEL_PASSWORD = Messages
        .getString("text_label_password");
    public final static String LABEL_REPEAT_PASSWORD = Messages
        .getString("text_label_repeat_password");

    public final static String ERROR_MESSAGE_PASSWORDS_NOT_MATCH = Messages
        .getString("error_message_passwords_not_match");
    public final static String ERROR_MESSAGE_COULD_NOT_CONNECT = Messages
        .getString("error_message_could_not_connect");
    public final static String ERROR_MESSAGE_NOT_CONNECTED_TO_SERVER = Messages
        .getString("error_message_not_connected_to_server");
    public final static String ERROR_MESSAGE_ACCOUNT_ALREADY_EXISTS = Messages
        .getString("error_message_account_already_exists");

    public final static String ERROR_MESSAGE_TOO_FAST_REGISTER_ACCOUNTS = Messages
        .getString("error_message_too_fast_register_accounts");

    public final static String GROUP_EXISTING_ACCOUNT = Messages
        .getString("group_existing_account");
    static public final String GROUP_TITLE_XMPP_JABBER_ACCOUNTS = Messages
        .getString("group_title_xmpp_jabber_accounts_in_shell-saros-preferences");

    static public final String BUTTON_ACTIVATE_ACCOUNT = Messages
        .getString("button_text_activate_account_in_shell-saros-preferences");

    static public final String BUTTON_EDIT_ACCOUNT = Messages
        .getString("button_edit_account");
    static public final String BUTTON_ADD_ACCOUNT = Messages
        .getString("button_text_add_account_in_shell-saros-preferences");
    static public final String BUTTON_DELETE_ACCOUNT = Messages
        .getString("button_text_delete_account_in_shell-saros-preferences");

    static public final String CHECKBOX_AUTO_CONNECT_ON_STARTUP = Messages
        .getString("checkbox_label_auto_connect_on_startup_in_shell-saros-preferences");

    static public final String CHECKBOX_DISABLE_VERSION_CONTROL = Messages
        .getString("checkbox_label_disable_version_control_support_in_shell-saros-preferences");

    static public final String CHECKBOX_ENABLE_CONCURRENT_UNDO = Messages
        .getString("checkbox_label_enable_concurrent_undo_shell-saros-preferences");

    static public final String CHECKBOX_START_FOLLOW_MODE = Messages
        .getString("checkbox_label_start_in_follow_mode_in_shell-saros-preferences");

    static public final String NODE_SAROS = Messages
        .getString("node_saros");
    static public final String NODE_SAROS_FEEDBACK = Messages
        .getString("node_Saros_feedback");
    static public final String NODE_SAROS_ADVANCED = Messages
        .getString("node_Saros_Advanced");
    static public final String NODE_SAROS_COMMUNICATION = Messages
        .getString("node_Saros_Communication");
    static public final String NODE_SAROS_SCREENSHARING = Messages
        .getString("node_Saros_screensharing");
    static public final String NODE_SAROS_SCREENSHARING_DESKTOP = Messages
        .getString("node_Saros_screensharing_desktop");
    static public final String NODE_SAROS_SCREENSHARING_ENCODER = Messages
        .getString("node_Saros_screensharing_encoder");
    static public final String NODE_SAROS_SCREENSHARING_ENCODER_IMAGE_TILE = Messages
        .getString("node_Saros_screensharing_encoder_image_tile");
    static public final String NODE_SAROS_SCREENSHARING_ENCODER_XUGGLER = Messages
        .getString("node_Saros_screensharing_encoder_xuggler");
    static public final String NODE_SAROS_SCREENSHARING_REMOTE_SCREEN_VIEW = Messages
        .getString("node_Saros_screensharing_remote_screen_view");

    /**********************************************
     * 
     * Saros View
     * 
     **********************************************/

    /*
     * View infos
     */
    public final static String VIEW_SAROS = Messages
        .getString("view_saros");
    public final static String VIEW_SAROS_ID = Messages
        .getString("view_saros_id");

    public final static String SHELL_REQUEST_OF_SUBSCRIPTION_RECEIVED = Messages
        .getString("shell_request_of_subscription_received");
    public final static String SHELL_BUDDY_ALREADY_ADDED = Messages
        .getString("shell_buddy_already_added");
    public final static String SHELL_BUDDY_LOOKUP_FAILED = Messages
        .getString("shell_buddy_look_up_failed");
    public final static String SHELL_SERVER_NOT_FOUND = Messages
        .getString("shell_server_not_found");
    public final static String SHELL_REMOVAL_OF_SUBSCRIPTION = Messages
        .getString("shell_removal_of_subscription");
    public final static String SHELL_SET_NEW_NICKNAME = Messages
        .getString("shell_set_new_nickname");
    public final static String SHELL_CONFIRM_CLOSING_SESSION = Messages
        .getString("shell_confirm_closing_session");
    public final static String SHELL_INCOMING_SCREENSHARING_SESSION = Messages
        .getString("shell_incoming_screensharing_session");
    public final static String SHELL_SCREENSHARING_ERROR_OCCURED = Messages
        .getString("shell_screensharing_an_error_occured");
    public final static String SHELL_INVITATION = Messages
        .getString("shell_invitation");
    public final static String SHELL_ADD_BUDDY = Messages
        .getString("shell_add_buddy");
    public final static String SHELL_ADD_BUDDY_TO_SESSION = Messages
        .getString("shell_add_buddy_to_session");
    public final static String SHELL_ERROR_IN_SAROS_PLUGIN = Messages
        .getString("shell_error_in_saros_plugin");
    public final static String SHELL_CLOSING_THE_SESSION = Messages
        .getString("close_the_session");
    public final static String SHELL_CONFIRM_LEAVING_SESSION = Messages
        .getString("comfirm_leaving_session");

    public final static String TB_DISCONNECT = Messages
        .getString("tb_disconnect");
    public final static String TB_ADD_A_NEW_BUDDY = Messages
        .getString("tb_add_a_new_buddy");
    public final static String TB_CONNECT = Messages
        .getString("tb_connect");
    public final static String TB_SHARE_SCREEN_WITH_BUDDY = Messages
        .getString("tb_share_screen_with_buddy");
    public final static String TB_STOP_SESSION_WITH_BUDDY = Messages
        .getString("tb_stop_session_with_user");
    public final static String TB_SEND_A_FILE_TO_SELECTED_BUDDY = Messages
        .getString("tb_send_a_file_to_selected_buddy");
    public final static String TB_START_VOIP_SESSION = Messages
        .getString("tb_start_a_voip_session");
    public final static String TB_NO_INCONSISTENCIES = Messages
        .getString("tb_no_inconsistencies");
    public final static String TB_INCONSISTENCY_DETECTED = Messages
        .getString("tb_inconsistency_detected_in");
    public final static String TB_ADD_BUDDY_TO_SESSION = Messages
        .getString("tb_add_buddy_to_session");
    public final static String TB_RESTRICT_INVITEES_TO_READ_ONLY_ACCESS = Messages
        .getString("tb_restrict_invitees_to_read_only_access");
    public final static String TB_ENABLE_DISABLE_FOLLOW_MODE = Messages
        .getString("tb_enable_disable_follow_mode");
    public final static String TB_LEAVE_SESSION = Messages
        .getString("tb_leave_session");
    public final static String TB_STOP_SESSION = Messages
        .getString("tb_stop_session");

    public final static String CM_DELETE = Messages.getString("cm_delete");
    public final static String CM_RENAME = Messages.getString("cm_rename");
    public final static String CM_SKYPE_THIS_BUDDY = Messages
        .getString("cm_skype_this_buddy");
    public final static String CM_ADD_TO_SAROS_SESSION = Messages
        .getString("cm_add_to_saros_session");
    public final static String CM_TEST_DATA_TRANSFER = Messages
        .getString("cm_test_data_transfer_connection");
    public final static String CM_GRANT_WRITE_ACCESS = Messages
        .getString("cm_grant_write_access");
    public final static String CM_RESTRICT_TO_READ_ONLY_ACCESS = Messages
        .getString("cm_restrict_to_read_only_access");
    public final static String CM_FOLLOW_PARTICIPANT = Messages
        .getString("cm_follow_this_buddy");
    public final static String CM_STOP_FOLLOWING = Messages
        .getString("cm_stop_following_this_buddy");
    public final static String CM_JUMP_TO_POSITION_SELECTED_BUDDY = Messages
        .getString("cm_jump_to_position_of_selected_buddy");
    public final static String CM_CHANGE_COLOR = Messages
        .getString("cm_change_color");
    public final static String CM_STOP_SAROS_SESSION = Messages
        .getString("cm_stop_saros_session");

    public final static String NODE_BUDDIES = Messages
        .getString("tree_item_label_buddies");
    public final static String NODE_SESSION = Messages
        .getString("tree_item_label_session");
    public final static String NODE_NO_SESSION_RUNNING = Messages
        .getString("tree_item_label_no_session_running");

    public final static String LABEL_XMPP_JABBER_JID = Messages
        .getString("text_label_xmpp_jabber_jid");

    public final static String GROUP_TITLE_CREATE_NEW_XMPP_JABBER_ACCOUNT = Messages
        .getString("group_title_create_new_xmpp_jabber_account");

    // Permission: Write Access
    static public final String OWN_PARTICIPANT_NAME = Messages
        .getString("own_participant_name");
    public final static String PERMISSION_NAME = Messages
        .getString("permission_name");

    /**********************************************
     * 
     * View Remote Screen
     * 
     **********************************************/
    // View infos
    public final static String VIEW_REMOTE_SCREEN = Messages
        .getString("view_remote_screen");
    public final static String VIEW_REMOTE_SCREEN_ID = Messages
        .getString("view_remote_screen_id");

    public final static String TB_CHANGE_MODE_IMAGE_SOURCE = Messages
        .getString("tb_change_mode_of_image_source");
    public final static String TB_STOP_RUNNING_SESSION = Messages
        .getString("tb_stop_running_session");
    public final static String TB_RESUME = Messages.getString("tb_resume");
    public final static String TB_PAUSE = Messages.getString("tb_pause");

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
    public final static String SHELL_INVITATION_CANCELLED = Messages
        .getString("shell_invitation_cancelled");
    public final static String SHELL_SESSION_INVITATION = Messages
        .getString("shell_session_invitation");
    public final static String SHELL_ADD_PROJECTS = Messages
        .getString("shell_add_projects");
    public final static String SHELL_ADD_PROJECT = Messages
        .getString("shell_add_project");
    public final static String SHELL_ADD_PROJECTS_TO_SESSION = Messages
        .getString("shell_add_projects_to_session");
    public final static String SHELL_PROBLEM_OCCURRED = Messages
        .getString("shell_problem_occurred");
    public final static String SHELL_WARNING_LOCAL_CHANGES_DELETED = Messages
        .getString("shell_warning_local_changes_deleted");
    public final static String SHELL_FOLDER_SELECTION = Messages
        .getString("shell_folder_selection");
    public final static String SHELL_SAVE_ALL_FILES_NOW = Messages
        .getString("shell_save_all_files_now");

    /* Context menu of a selected tree item on the package explorer view */
    public final static String CM_SHARE_WITH = Messages
        .getString("cm_share_with");

    public final static String CM_WORK_TOGETHER_ON = Messages
        .getString("cm_work_together_on");
    public final static String CM_MULTIPLE_BUDDIES = Messages
        .getString("cm_multiple_buddies");

    public final static String CM_MULTIPLE_PROJECTS = Messages
        .getString("cm_multiple_projects");

    // public final static String CM_SHARE_PROJECT = SarosMessages
    // .getString("cm_share_project");
    public final static String CM_ADD_TO_SESSION = Messages
        .getString("cm_add_to_session");

    /*
     * second page of the wizard "Session invitation"
     */
    public final static String RADIO_USING_EXISTING_PROJECT = Messages
        .getString("radio_use_existing_project");
    public final static String RADIO_CREATE_NEW_PROJECT = Messages
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

    /**********************************************
     * 
     * Others
     * 
     **********************************************/
    public final static String STRING_REGEX_WITH_LINE_BREAK = ".*\n*.*";
    public final static String PKG_REGEX = "[\\w*\\.]*\\w*";
    public final static String PROJECT_REGEX = "\\w*";

    public final static String SCREENSHOTDIR = "test/STF/screenshot";

    public final static Map<String, String> viewTitlesAndIDs = new HashMap<String, String>();
    static {
        viewTitlesAndIDs.put(VIEW_PACKAGE_EXPLORER, VIEW_PACKAGE_EXPLORER_ID);
        viewTitlesAndIDs.put(VIEW_REMOTE_SCREEN, VIEW_REMOTE_SCREEN_ID);
        viewTitlesAndIDs.put(VIEW_SAROS, VIEW_SAROS_ID);
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
        // the name of project in SVN_control contains special characters, which
        // should be filtered.
        String[] names = text.split(" ");
        if (names.length > 1) {
            text = names[0];
        }
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
