/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.fu_berlin.inf.dpp.preferences;

/**
 * Constant definitions for plug-in preferences
 * 
 * @author rdjemili
 * @author coezbek
 */
public class PreferenceConstants {

    public static final String ACTIVE_ACCOUNT = "active_account";

    public static final String ACCOUNT_DATA = "account_data";

    public static final String AUTO_CONNECT = "autoconnect";

    public static final String AUTO_PORTMAPPING_DEVICEID = "autoportmappingdeviceid";

    public static final String AUTO_PORTMAPPING_LASTMAPPEDPORT = "autoportmappinglastmappedport";

    public static final String SKYPE_USERNAME = "skypename";

    public static final String CONCURRENT_UNDO = "concurrent_undo";

    public static final String DEBUG = "debug";

    public static final String FILE_TRANSFER_PORT = "port";

    public static final String USE_NEXT_PORTS_FOR_FILE_TRANSFER = "use_next_ports_for_file_transfer";

    public static final String FORCE_FILETRANSFER_BY_CHAT = "chatfiletransfer";

    public static final String GATEWAYCHECKPERFORMED = "gatewaycheckperformed";

    public static final String LOCAL_SOCKS5_PROXY_DISABLED = " local_socks5_proxy_disabled";

    public static final String LOCAL_SOCKS5_PROXY_USE_UPNP_EXTERNAL_ADDRESS = "local_socks5_proxy_use_upnp_external_address";

    public static final String LOCAL_SOCKS5_PROXY_CANDIDATES = "local_socks5_proxy_candidates";

    public static final String STUN = "stun_server";

    public static final String STUN_PORT = "stun_server_port";

    public static final String ENABLE_BALLOON_NOTIFICATION = "enable_balloon_notification";

    public static final String DISABLE_VERSION_CONTROL = "disable_version_control";

    public static final String SERVER_ACTIVATED = "server_activated";

    /*
     * Preferences of the feedback preferences page
     * 
     * These preferences are kept both in the workspace scope and globally (in
     * the configuration).
     */

    /** Can be set and read by the FeedbackManager */
    public static final String FEEDBACK_SURVEY_DISABLED = "feedback.survey.disabled";

    /** Can be set and read by the FeedbackManager */
    public static final String FEEDBACK_SURVEY_INTERVAL = "feedback.survey.interval";

    /** Can be set and read by the StatisticManager */
    public static final String STATISTIC_ALLOW_SUBMISSION = "statistic.allow.submission";

    /** Can be set and read by the ErrorLogManager */
    public static final String ERROR_LOG_ALLOW_SUBMISSION = "error.log.allow.submission";

    /** Can be set and read by the ErrorLogManager */
    public static final String ERROR_LOG_ALLOW_SUBMISSION_FULL = "error.log.allow.submission.full";

    /*
     * Global preferences, not initialized i.e. no default values
     */

    /** Can be set and read by the StatisticManager */
    public static final String SESSION_COUNT = "session.count";

    /** Can be set and read by the FeedbackManager */
    public static final String SESSIONS_UNTIL_NEXT = "sessions.until.next.survey";

    /** Can be read by the StatisticManager */
    public static final String RANDOM_USER_ID = "user.id";

    /**
     * Preference used for a way to let the user identify himself.
     * 
     * For instance, this might get a value such as "coezbek" or "rdjemili".
     */
    public static final String STATISTICS_PSEUDONYM_ID = "STATISTICS_PSEUDONYM_ID";

    /**
     * Preference used to let the user declare whether he wants a self defined
     * pseudonym be transmitted during statistics or error log transmission
     */
    public static final String STATISTIC_ALLOW_PSEUDONYM = "STATISTIC_ALLOW_PSEUDONYM";

    /*
     * Preferences for Communication /Chat
     */

    public static final String USE_IRC_STYLE_CHAT_LAYOUT = "chat.irc.layout";

    public static final String CUSTOM_MUC_SERVICE = "custom_muc_service";

    public static final String FORCE_CUSTOM_MUC_SERVICE = "force_custom_muc_service";

    /* Sound Events */

    public static final String SOUND_ENABLED = "sound.enabled";

    public static final String SOUND_PLAY_EVENT_MESSAGE_SENT = "sound.play.event.message.sent";

    public static final String SOUND_PLAY_EVENT_MESSAGE_RECEIVED = "sound.play.event.message.received";

    public static final String SOUND_PLAY_EVENT_CONTACT_ONLINE = "sound.play.event.contact.online";

    public static final String SOUND_PLAY_EVENT_CONTACT_OFFLINE = "sound.play.event.contact.offline";

    /*
     * Wizard options that need to be permanently saved
     */
    public static final String PROJECTSELECTION_FILTERCLOSEDPROJECTS = "projectselection.filterClosedProjects";
    public static final String CONTACT_SELECTION_FILTER_NON_SAROS_CONTACTS = "projectselection.filterNonSarosBuddies";

    /*
     * Preferences to remember the saros view settings
     */
    public static final String SAROSVIEW_SASH_WEIGHT_LEFT = "ui.sarosview.sashweight.left";
    public static final String SAROSVIEW_SASH_WEIGHT_RIGHT = "ui.sarosview.sashweight.right";

    /*
     * Stop sessions when alone as host?
     */
    public static final String AUTO_STOP_EMPTY_SESSION = "auto.stop.empty.session";

    /**
     * color ID that should be used in a session if it is not already occupied
     */
    public static final String FAVORITE_SESSION_COLOR_ID = "favorite.session.color.id";

    /*
     * Annotation stuff for the editor
     */

    public static final String SHOW_CONTRIBUTION_ANNOTATIONS = "editor.annotation.contribution.enabled";

    public static final String SHOW_SELECTIONFILLUP_ANNOTATIONS = "editor.annotation.selectionfillup.enabled";

    /*
     * ConsoleSharing
     */
    public static final String CONSOLESHARING_ENABLED = "consolesharing.enabled";

}
