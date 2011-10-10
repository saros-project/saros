package de.fu_berlin.inf.dpp.project;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "de.fu_berlin.inf.dpp.project.messages"; //$NON-NLS-1$
    public static String ChangeColorManager_buddy_no_participant;
    public static String ChangeColorManager_color_conflict;
    public static String PermissionManager_buddy_joined;
    public static String PermissionManager_buddy_joined_text;
    public static String PermissionManager_buddy_left;
    public static String PermissionManager_buddy_left_text;
    public static String PermissionManager_buddy_no_participant;
    public static String PermissionManager_he_has_now_access;
    public static String PermissionManager_permission_changed;
    public static String PermissionManager_read_only;
    public static String PermissionManager_write;
    public static String PermissionManager_you_have_now_access;
    public static String ProjectsAddedManager_user_no_participant_of_session;
    public static String ResourceChangeValidator_error_leave_session_before_delete_project;
    public static String ResourceChangeValidator_error_no_write_access;
    public static String SarosSession_jids_should_be_resource_qualified;
    public static String SarosSession_only_inviter_can_initate_permission_changes;
    public static String SarosSession_performing_permission_change;
    public static String SarosSessionManager_canceled_invitation;
    public static String SarosSessionManager_canceled_invitation_text;
    public static String SarosSessionManager_creating_file_list;
    public static String SarosSessionManager_error_during_invitation;
    public static String SarosSessionManager_error_during_invitation_text;
    public static String SarosSessionManager_error_during_invitation_text2;
    public static String SarosSessionManager_inviting_user;
    public static String SarosSessionManager_no_connection;
    public static String SarosSessionManager_project_sharing_cancelled;
    public static String SarosSessionManager_project_sharing_cancelled_text;
    public static String SarosSessionManager_sharing_project;
    public static String SarosSessionManager_sharing_project_cancelled_remotely;
    public static String SarosSessionManager_sharing_project_cancelled_remotely_text;
    public static String SharedProject_path_is_null;
    public static String SharedProject_resource_in_map_not_exist;
    public static String SharedProject_resource_is_null;
    public static String SharedProject_resource_map_does_not_contain;
    public static String SharedProject_resource_not_in_map;
    public static String SharedProject_resource_not_in_project;
    public static String SharedProject_revision_out_of_sync;
    public static String SharedProject_vcs_url_out_of_sync;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
