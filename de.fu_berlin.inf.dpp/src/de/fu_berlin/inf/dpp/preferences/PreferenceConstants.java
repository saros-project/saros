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

    public static final String ENCRYPT_ACCOUNT = "encrypt_account";

    public static final String AUTO_CONNECT = "autoconnect";

    public static final String AUTO_PORTMAPPING_DEVICEID = "autoportmappingdeviceid";

    public static final String AUTO_PORTMAPPING_LASTMAPPEDPORT = "autoportmappinglastmappedport";

    public static final String NEEDS_BASED_SYNC = "undefined";

    public static final String SKYPE_USERNAME = "skypename";

    public static final String CONCURRENT_UNDO = "concurrent_undo";

    public static final String DEBUG = "debug";

    public static final String FILE_TRANSFER_PORT = "port";

    public static final String USE_NEXT_PORTS_FOR_FILE_TRANSFER = "use_next_ports_for_file_transfer";

    public static final String FORCE_FILETRANSFER_BY_CHAT = "chatfiletransfer";

    public static final String GATEWAYCHECKPERFORMED = "gatewaycheckperformed";

    public static final String LOCAL_SOCKS5_PROXY_DISABLED = " local_socks5_proxy_disabled";

    public static final String STUN = "stun_server";

    public static final String STUN_PORT = "stun_server_port";

    public static final String ENABLE_BALLOON_NOTIFICATION = "enable_balloon_notification";

    public static final String SKIP_SYNC_SELECTABLE = "invitation.dialog.skip.enabled";

    public static final String DISABLE_VERSION_CONTROL = "disable_version_control";

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

    /**
     * Preferences for Communication
     */

    /** Custom multiuser-chat service name **/
    public static final String CUSTOM_MUC_SERVICE = "custom_muc_service";

    public static final String FORCE_CUSTOM_MUC_SERVICE = "force_custom_muc_service";

    public static final String SOUND_ENABLED = "sound.enabled";

    public static final String VOIP_ENABLED = "voip.enabled";

    /** Audio quality level (1-10, 10 is best) **/
    public static final String AUDIO_QUALITY_LEVEL = "AUDIO_QUALITY_LEVEL";

    /** Checkbox if variable bit rate encoding is used */
    public static final String AUDIO_VBR = "AUDIO_VBR";

    /** Sample rate for audio encoder **/
    public static final String AUDIO_SAMPLERATE = "AUDIO_SAMPLERATE";

    /** Checkbox if discontinues transmission transmission is used */
    public static final String AUDIO_ENABLE_DTX = "AUDIO_ENABLE_DTX";

    /** Record device for VoIP **/
    public static final String AUDIO_RECORD_DEVICE = "AUDIO_RECORD_DEVICE";

    /** Playback device for VoIP **/
    public static final String AUDIO_PLAYBACK_DEVICE = "AUDIO_PLAYBACK_DEVICE";

    /*
     * VideoSharing
     */

    public static final String VIDEOSHARING_ENABLED = "vs.enabled";

    /*
     * encoder constants
     */

    public static final String ENCODING_VIDEO_FRAMERATE = "vs.encoding.framerate";
    public static final String ENCODING_VIDEO_RESOLUTION = "vs.encoding.resolution";
    public static final String ENCODING_VIDEO_WIDTH = "vs.encoding.width";
    public static final String ENCODING_VIDEO_HEIGHT = "vs.encoding.height";
    public static final String ENCODING_MAX_BITRATE = "vs.encoding.max_bitrate";
    public static final String ENCODING_MAX_BITRATE_COMBO = "vs.encoding.max_bitrate_combo";
    public static final String ENCODING_CODEC = "vs.encoding.codec";

    /*
     * xuggler encoder
     */

    public static final String XUGGLER_CONTAINER_FORMAT = "vs.encoding.xuggler.containerformat";
    public static final String XUGGLER_CODEC = "vs.encoding.xuggler.codec";
    public static final String XUGGLER_USE_VBV = "vs.encoding.xuggler.vbv";

    /*
     * tile encoder
     */

    public static final String IMAGE_TILE_CODEC = "vs.encoding.imagetile.codec";
    public static final String IMAGE_TILE_QUALITY = "vs.encoding.imagetile.quality";
    public static final String IMAGE_TILE_COLORS = "vs.encoding.imagetile.colors";
    public static final String IMAGE_TILE_SERPENTINE = "vs.encoding.imagetile.serpentine";
    public static final String IMAGE_TILE_DITHER = "vs.encoding.imagetile.dither";

    /*
     * player
     */

    public static final String PLAYER_RESAMPLE = "vs.player.resample";
    public static final String PLAYER_KEEP_ASPECT_RATIO = "vs.player.ratio";

    /*
     * screen
     */

    public static final String SCREEN_INITIAL_MODE = "vs.source.screen.initial_mode";
    public static final String SCREEN_MOUSE_AREA_QUALITY = "vs.source.mouse_area_quality";
    public static final String SCREEN_MOUSE_AREA_WIDTH = "vs.source.screen.mouse_area_width";
    public static final String SCREEN_MOUSE_AREA_HEIGHT = "vs.source.screen.mouse_area_height";
    public static final String SCREEN_SHOW_MOUSEPOINTER = "vs.source.screen.show_mouse_pointer";

    /*
     * Wizard options that need to be permanently saved
     */
    public static final String PROJECTSELECTION_FILTERCLOSEDPROJECTS = "projectselection.filterClosedProjects";
    public static final String BUDDYSELECTION_FILTERNONSAROSBUDDIES = "projectselection.filterNonSarosBuddies";

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
}
