package de.fu_berlin.inf.dpp.core.project;

import de.fu_berlin.inf.dpp.intellij.util.MessageUtils;

public class Messages {

    private static final String BUNDLE_NAME = Messages.class.getName()
        .toLowerCase();

    public static String ResourceChangeValidator_error_leave_session_before_delete_project;
    public static String ResourceChangeValidator_error_no_write_access;

    public static String SarosSession_performing_permission_change;
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
        MessageUtils.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
