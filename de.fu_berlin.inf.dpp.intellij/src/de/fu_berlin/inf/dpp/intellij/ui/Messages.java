package de.fu_berlin.inf.dpp.intellij.ui;

import de.fu_berlin.inf.dpp.intellij.util.MessageUtils;

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
  public static String AddProjectToSessionWizard_module_not_found_title;
  public static String AddProjectToSessionWizard_module_not_found_message_condition;
  public static String AddProjectToSessionWizard_invalid_module_title;
  public static String AddProjectToSessionWizard_invalid_module_message_condition;
  public static String AddProjectToSessionWizard_error_creating_module_object_title;
  public static String AddProjectToSessionWizard_error_creating_module_object_message;

  public static String CollaborationUtils_confirm_closing;
  public static String CollaborationUtils_confirm_closing_text;
  public static String CollaborationUtils_confirm_leaving;
  public static String CollaborationUtils_confirm_leaving_text;
  public static String CollaborationUtils_insufficient_privileges;
  public static String CollaborationUtils_insufficient_privileges_text;
  public static String CollaborationUtils_partial;

  public static String ConsistencyAction_confirm_dialog_title;
  public static String ConsistencyAction_message_inconsistency_detected;
  public static String ConsistencyAction_message_inconsistency_detected_no_files;
  public static String ConsistencyAction_progress_perform_recovery;
  public static String ConsistencyAction_title_inconsistency_detected;
  public static String ConsistencyAction_tooltip_inconsistency_detected;
  public static String ConsistencyAction_tooltip_no_inconsistency;

  public static String NegotiationHandler_canceled_invitation;
  public static String NegotiationHandler_canceled_invitation_text;
  public static String NegotiationHandler_error_during_invitation;
  public static String NegotiationHandler_error_during_invitation_text;
  public static String NegotiationHandler_inviting_user;
  public static String NegotiationHandler_project_sharing_canceled_text;
  public static String NegotiationHandler_sharing_project;
  public static String NegotiationHandler_sharing_project_canceled_remotely;
  public static String NegotiationHandler_sharing_project_canceled_remotely_text;

  public static String EnterProjectNamePage_create_new_project;
  public static String EnterProjectNamePage_project_name;
  public static String EnterProjectNamePage_title2;
  public static String EnterProjectNamePage_description;
  public static String EnterProjectNamePage_description_changed_files;
  public static String EnterProjectNamePage_use_existing_project;

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

  public static String ConnectButton_connect_to_new_account_title;
  public static String ConnectButton_connect_to_new_account_message;

  public static String Contact_saros_message_conditional;

  public static String ContactPopMenu_unsupported_ide_title;
  public static String ContactPopMenu_unsupported_ide_message_condition;
  public static String ContactPopMenu_module_not_found_title;
  public static String ContactPopMenu_module_not_found_message_condition;
  public static String ContactPopMenu_invalid_module_title;
  public static String ContactPopMenu_invalid_module_message_condition;
  public static String ContactPopMenu_error_creating_module_object_title;
  public static String ContactPopMenu_error_creating_module_object_message;

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

  private Messages() {}
}
