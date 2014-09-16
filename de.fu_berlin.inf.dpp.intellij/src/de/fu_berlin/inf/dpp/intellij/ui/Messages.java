/*
 *
 *  DPP - Serious Distributed Pair Programming
 *  (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2010
 *  (c) NFQ (www.nfq.com) - 2014
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 1, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * /
 */

package de.fu_berlin.inf.dpp.intellij.ui;

import de.fu_berlin.inf.dpp.intellij.util.MessageUtils;

/**
 * UI message bundle.
 */
public class Messages {

    private static final String BUNDLE_NAME = Messages.class.getName()
        .toLowerCase();

    static {
        MessageUtils.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    public static String AddProjectToSessionWizard_title;

    public static String CollaborationUtils_confirm_closing;
    public static String CollaborationUtils_confirm_closing_text;
    public static String CollaborationUtils_confirm_leaving;
    public static String CollaborationUtils_confirm_leaving_text;
    public static String CollaborationUtils_insufficient_privileges;
    public static String CollaborationUtils_insufficient_privileges_text;
    public static String CollaborationUtils_partial;

    public static String ConsistencyAction_confirm_dialog_title;
    public static String ConsistencyAction_message_inconsistency_detected;
    public static String ConsistencyAction_progress_perform_recovery;
    public static String ConsistencyAction_title_inconsistency_detected;
    public static String ConsistencyAction_tooltip_inconsistency_detected;
    public static String ConsistencyAction_tooltip_no_inconsistency;

    public static String NegotiationHandler_canceled_invitation;
    public static String NegotiationHandler_canceled_invitation_text;
    public static String NegotiationHandler_error_during_invitation;
    public static String NegotiationHandler_error_during_invitation_text;
    public static String NegotiationHandler_inviting_user;
    public static String NegotiationHandler_project_sharing_cancelled_text;
    public static String NegotiationHandler_sharing_project;
    public static String NegotiationHandler_sharing_project_cancelled_remotely;
    public static String NegotiationHandler_sharing_project_cancelled_remotely_text;

    public static String EnterProjectNamePage_create_new_project;
    public static String EnterProjectNamePage_project_name;
    public static String EnterProjectNamePage_title2;
    public static String EnterProjectNamePage_use_existing_project;

    public static String ShowDescriptionPage_description;
    public static String ShowDescriptionPage_title2;

    public static String JoinSessionWizard_8;
    public static String JoinSessionWizard_accept;
    public static String JoinSessionWizard_info;
    public static String JoinSessionWizard_inv_cancelled;
    public static String JoinSessionWizard_inv_cancelled_text;
    public static String JoinSessionWizard_inv_cancelled_text2;
    public static String JoinSessionWizard_inv_cancelled_text3;
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

    private Messages() {
    }
}
