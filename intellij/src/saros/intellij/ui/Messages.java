package saros.intellij.ui;

import saros.intellij.util.MessageUtils;

/** UI message bundle. */
public class Messages {

  private static final String BUNDLE_NAME = Messages.class.getName().toLowerCase();

  static {
    MessageUtils.initializeMessages(BUNDLE_NAME, Messages.class);
  }

  public static String AddProjectToSessionWizard_title;
  public static String AddProjectToSessionWizard_module_creation_failed_title;
  public static String AddProjectToSessionWizard_module_creation_failed_message_condition;
  public static String AddProjectToSessionWizard_module_already_exists_title;
  public static String AddProjectToSessionWizard_module_already_exists_message_condition;
  public static String AddProjectToSessionWizard_invalid_module_title;
  public static String AddProjectToSessionWizard_invalid_module_message_condition;
  public static String AddProjectToSessionWizard_error_reading_module_selection_result_title;
  public static String AddProjectToSessionWizard_error_reading_module_selection_result_message;
  public static String AddProjectToSessionWizard_title2;
  public static String AddProjectToSessionWizard_description;
  public static String AddProjectToSessionWizard_description_changed_files;
  public static String AddProjectToSessionWizard_no_module_type_received_title;
  public static String AddProjectToSessionWizard_no_module_type_received_message;
  public static String AddProjectToSessionWizard_no_session_title;
  public static String AddProjectToSessionWizard_no_session_message;
  public static String AddProjectToSessionWizard_context_teardown_title;
  public static String AddProjectToSessionWizard_context_teardown_message;

  public static String CollaborationUtils_confirm_closing;
  public static String CollaborationUtils_confirm_closing_text;
  public static String CollaborationUtils_confirm_leaving;
  public static String CollaborationUtils_confirm_leaving_text;
  public static String CollaborationUtils_insufficient_privileges;
  public static String CollaborationUtils_insufficient_privileges_text;
  public static String CollaborationUtils_partial;

  public static String ConsistencyAction_progress_perform_recovery;

  public static String ConsistencyButton_confirm_dialog_title;
  public static String ConsistencyButton_confirm_dialog_message;
  public static String ConsistencyButton_title_inconsistency_detected;
  public static String ConsistencyButton_message_inconsistency_detected;
  public static String ConsistencyButton_message_inconsistency_detected_no_files;
  public static String ConsistencyButton_title_no_inconsistencies_remaining;
  public static String ConsistencyButton_message_no_inconsistencies_remaining;
  public static String ConsistencyButton_tooltip_functionality;
  public static String ConsistencyButton_tooltip_inconsistency_detected;
  public static String ConsistencyButton_tooltip_no_inconsistency;
  public static String ConsistencyButton_inconsistent_list_module;
  public static String ConsistencyButton_inconsistent_list_file;

  public static String FollowButton_tooltip;
  public static String FollowButton_user_entry_prefix;
  public static String FollowButton_leave_follow_mode_entry;

  public static String LeaveSessionButton_tooltip;

  public static String ConnectButton_tooltip;
  public static String ConnectButton_disconnect;
  public static String ConnectButton_configure_accounts;
  public static String ConnectButton_add_account;
  public static String ConnectButton_account_creation_jid_title;
  public static String ConnectButton_account_creation_jid_initial_input;
  public static String ConnectButton_account_creation_jid_message;
  public static String ConnectButton_account_creation_invalid_jid_title;
  public static String ConnectButton_account_creation_invalid_jid_message;
  public static String ConnectButton_account_creation_failed_title;
  public static String ConnectButton_account_creation_failed_message;
  public static String ConnectButton_account_creation_password_title;
  public static String ConnectButton_account_creation_password_message;
  public static String ConnectButton_account_creation_invalid_password_title;
  public static String ConnectButton_account_creation_invalid_password_message;
  public static String ConnectButton_account_creation_xmpp_server_title;
  public static String ConnectButton_account_creation_xmpp_server_initial_input;
  public static String ConnectButton_account_creation_xmpp_server_message;
  public static String ConnectButton_account_creation_xmpp_server_port_title;
  public static String ConnectButton_account_creation_xmpp_server_port_initial_input;
  public static String ConnectButton_account_creation_xmpp_server_port_message;
  public static String ConnectButton_account_creation_xmpp_server_invalid_port_title;
  public static String ConnectButton_account_creation_xmpp_server_invalid_port_message;
  public static String ConnectButton_connect_to_new_account_title;
  public static String ConnectButton_connect_to_new_account_message;

  public static String NegotiationHandler_canceled_invitation;
  public static String NegotiationHandler_canceled_invitation_text;
  public static String NegotiationHandler_error_during_invitation;
  public static String NegotiationHandler_error_during_invitation_text;
  public static String NegotiationHandler_inviting_user;
  public static String NegotiationHandler_project_sharing_canceled_text;
  public static String NegotiationHandler_sharing_project;
  public static String NegotiationHandler_sharing_project_canceled_remotely;
  public static String NegotiationHandler_sharing_project_canceled_remotely_text;

  public static String ShowDescriptionPage_description;
  public static String ShowDescriptionPage_title2;

  public static String JoinSessionWizard_8;
  public static String JoinSessionWizard_accept;
  public static String JoinSessionWizard_info;
  public static String JoinSessionWizard_inv_canceled;
  public static String JoinSessionWizard_inv_canceled_text;
  public static String JoinSessionWizard_inv_canceled_text2;
  public static String JoinSessionWizard_inv_canceled_text3;
  public static String JoinSessionWizard_title;

  public static String UserStatusChangeHandler_user_joined;
  public static String UserStatusChangeHandler_user_joined_text;
  public static String UserStatusChangeHandler_user_left;
  public static String UserStatusChangeHandler_user_left_text;
  public static String UserStatusChangeHandler_he_has_now_access;
  public static String UserStatusChangeHandler_permission_changed;
  public static String UserStatusChangeHandler_read_only;
  public static String UserStatusChangeHandler_write;
  public static String UserStatusChangeHandler_you_have_now_access;

  public static String SubscriptionManager_incoming_subscription_request_title;
  public static String SubscriptionManager_incoming_subscription_request_message;

  public static String Contact_saros_message_conditional;

  public static String ContactPopMenu_root_popup_text;
  public static String ContactPopMenu_menu_tooltip_share_module;
  public static String ContactPopMenu_menu_tooltip_project_module;
  public static String ContactPopMenu_menu_tooltip_invalid_module;
  public static String ContactPopMenu_menu_entry_error_processing_project;
  public static String ContactPopMenu_menu_entry_no_modules_found;
  public static String ContactPopMenu_menu_entry_no_valid_modules_found;
  public static String ContactPopMenu_unsupported_ide_title;
  public static String ContactPopMenu_unsupported_ide_message_condition;
  public static String ContactPopMenu_module_not_found_title;
  public static String ContactPopMenu_module_not_found_message_condition;

  public static String ShareWithUserAction_description;

  public static String SessionStatusChangeHandler_session_started_title;
  public static String SessionStatusChangeHandler_session_started_host_message;
  public static String SessionStatusChangeHandler_session_started_host_empty_message;
  public static String SessionStatusChangeHandler_session_started_client_message;
  public static String SessionStatusChangeHandler_session_ended_title;
  public static String SessionStatusChangeHandler_session_ended_message;
  public static String SessionStatusChangeHandler_local_user_left;
  public static String SessionStatusChangeHandler_host_left;
  public static String SessionStatusChangeHandler_kicked;
  public static String SessionStatusChangeHandler_connection_lost;

  public static String LocalEditorManipulator_incompatible_encoding_title;
  public static String LocalEditorManipulator_incompatible_encoding_message;

  public static String FollowModeNotificationDispatcher_started_following_title;
  public static String FollowModeNotificationDispatcher_started_following_message;
  public static String FollowModeNotificationDispatcher_stopped_following_title;
  public static String FollowModeNotificationDispatcher_stopped_following_message;
  public static String FollowModeNotificationDispatcher_end_reason_FOLLOWEE_LEFT_SESSION;
  public static String
      FollowModeNotificationDispatcher_end_reason_FOLLOWER_CLOSED_OR_SWITCHED_EDITOR;
  public static String FollowModeNotificationDispatcher_end_reason_FOLLOWER_CLOSED_EDITOR;
  public static String FollowModeNotificationDispatcher_end_reason_FOLLOWER_STOPPED;
  public static String FollowModeNotificationDispatcher_end_reason_FOLLOWER_SWITCHES_FOLLOWEE;

  public static String ModuleTab_project_label;
  public static String ModuleTab_create_new_module;
  public static String ModuleTab_create_new_module_name;
  public static String ModuleTab_create_new_module_base_path;
  public static String ModuleTab_module_base_path_file_chooser_title;
  public static String ModuleTab_module_base_path_file_chooser_description;
  public static String ModuleTab_use_existing_module;
  public static String ModuleTab_use_existing_module_local_module;
  public static String ModuleTab_create_new_module_name_invalid_tooltip;
  public static String ModuleTab_create_new_module_base_path_invalid_tooltip;
  public static String ModuleTab_use_existing_module_local_module_invalid_tooltip;

  public static String ColorPreferences_display_name;
  public static String ColorPreferences_text_selection_attribute_display_name;
  public static String ColorPreferences_text_contribution_attribute_display_name;
  public static String ColorPreferences_default_user_text_selection_attribute_display_name;
  public static String ColorPreferences_default_user_text_contribution_attribute_display_name;
  public static String ColorPreferences_default_user_description;
  public static String ColorPreferences_user_description;
  public static String ColorPreferences_user_example_text_contribution;
  public static String ColorPreferences_user_example_text_selection;

  private Messages() {}
}
