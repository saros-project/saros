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

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.IPreferenceStore;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.feedback.AbstractFeedbackManager;
import de.fu_berlin.inf.dpp.feedback.FeedbackManager;
import de.fu_berlin.inf.dpp.videosharing.VideoSharing;
import de.fu_berlin.inf.dpp.videosharing.preferences.VideoSharingPreferenceHelper;
import de.fu_berlin.inf.dpp.videosharing.source.Screen;

/**
 * Class used to initialize default preference values.
 * 
 * @author rdjemili
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

    /*
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer
     */
    protected static final Logger log = Logger
        .getLogger(PreferenceInitializer.class.getName());

    @Override
    public void initializeDefaultPreferences() {
        IEclipsePreferences prefs = new DefaultScope().getNode(Saros.SAROS);
        setPreferences(prefs);
    }

    public static void setPreferences(IEclipsePreferences prefs) {
        setPreferences(new IEclipsePreferencesWrapper(prefs));
    }

    public static void setPreferences(IPreferenceStore preferenceStore) {
        setPreferences(new IPreferenceStoreWrapper(preferenceStore));
    }

    private static void setPreferences(PreferenceHolderWrapper prefs) {
        prefs.setValue(PreferenceConstants.GETTING_STARTED_FINISHED, false);

        prefs.setValue(PreferenceConstants.ENCRYPT_ACCOUNT, false);
        prefs.setValue(PreferenceConstants.AUTO_CONNECT, true);
        prefs.setValue(PreferenceConstants.AUTO_PORTMAPPING_DEVICEID, "");
        prefs.setValue(PreferenceConstants.GATEWAYCHECKPERFORMED, false);
        prefs.setValue(PreferenceConstants.AUTO_FOLLOW_MODE, false);
        prefs.setValue(PreferenceConstants.NEEDS_BASED_SYNC, "undefined");
        prefs.setValue(PreferenceConstants.SKYPE_USERNAME, "");
        prefs.setValue(PreferenceConstants.DEBUG, false);
        prefs.setValue(PreferenceConstants.FILE_TRANSFER_PORT, 7777);
        prefs.setValue(PreferenceConstants.USE_NEXT_PORTS_FOR_FILE_TRANSFER,
            true);
        prefs.setValue(PreferenceConstants.LOCAL_SOCKS5_PROXY_DISABLED, false);
        prefs.setValue(PreferenceConstants.FORCE_FILETRANSFER_BY_CHAT, false);

        prefs.setValue(PreferenceConstants.STUN, "stunserver.org");
        prefs.setValue(PreferenceConstants.STUN_PORT, 3478);
        prefs.setValue(PreferenceConstants.MILLIS_UPDATE, 300);
        prefs.setValue(PreferenceConstants.AUTO_ACCEPT_INVITATION, false);
        prefs.setValue(PreferenceConstants.CONCURRENT_UNDO, false);
        prefs.setValue(PreferenceConstants.PING_PONG, false);
        prefs.setValue(PreferenceConstants.DISABLE_VERSION_CONTROL, false);

        // XMPP

        prefs.setValue(PreferenceConstants.DEFAULT_XMPP_PORT, 5222);

        // Advanced Preferences
        prefs.setValue(PreferenceConstants.SKIP_SYNC_SELECTABLE, false);
        prefs.setValue(PreferenceConstants.ENABLE_BALLOON_NOTIFICATION, true);

        // InvitationDialog
        prefs.setValue(PreferenceConstants.AUTO_CLOSE_DIALOG, true);
        prefs.setValue(
            PreferenceConstants.BUDDYSELECTION_FILTERNONSAROSBUDDIES, true);

        // its a new workspace per default, is set to false after first start in
        // earlyStartup()
        prefs.setValue(PreferenceConstants.NEW_WORKSPACE, true);

        // Initialize Feedback Preferences
        prefs.setValue(PreferenceConstants.FEEDBACK_SURVEY_DISABLED,
            FeedbackManager.FEEDBACK_ENABLED);
        prefs.setValue(PreferenceConstants.FEEDBACK_SURVEY_INTERVAL, 5);
        prefs.setValue(PreferenceConstants.STATISTIC_ALLOW_SUBMISSION,
            AbstractFeedbackManager.UNKNOWN);
        prefs.setValue(PreferenceConstants.ERROR_LOG_ALLOW_SUBMISSION,
            AbstractFeedbackManager.UNKNOWN);
        prefs.setValue(PreferenceConstants.ERROR_LOG_ALLOW_SUBMISSION_FULL,
            AbstractFeedbackManager.FORBID);

        // Communication default settings
        prefs.setValue(PreferenceConstants.CHATSERVER,
            "conference.saros-con.imp.fu-berlin.de");
        prefs.setValue(PreferenceConstants.USE_DEFAULT_CHATSERVER, true);
        prefs.setValue(PreferenceConstants.BEEP_UPON_IM, true);
        prefs.setValue(PreferenceConstants.AUDIO_VBR, true);
        prefs.setValue(PreferenceConstants.AUDIO_ENABLE_DTX, true);
        prefs.setValue(PreferenceConstants.AUDIO_SAMPLERATE, "44100");
        prefs.setValue(PreferenceConstants.AUDIO_QUALITY_LEVEL, "8");

        // videosharing

        prefs.setValue(PreferenceConstants.ENCODING_VIDEO_FRAMERATE, 5);
        prefs.setValue(PreferenceConstants.ENCODING_VIDEO_RESOLUTION,
            VideoSharingPreferenceHelper.RESOLUTIONS[2][1]);
        prefs.setValue(PreferenceConstants.ENCODING_VIDEO_WIDTH, 320);
        prefs.setValue(PreferenceConstants.ENCODING_VIDEO_HEIGHT, 240);
        prefs.setValue(PreferenceConstants.ENCODING_MAX_BITRATE, 512000);
        prefs.setValue(PreferenceConstants.ENCODING_CODEC,
            VideoSharing.Codec.IMAGE.name());

        prefs.setValue(PreferenceConstants.XUGGLER_CONTAINER_FORMAT, "flv");
        prefs.setValue(PreferenceConstants.XUGGLER_CODEC, "libx264");
        prefs.setValue(PreferenceConstants.XUGGLER_USE_VBV, false);

        prefs.setValue(PreferenceConstants.IMAGE_TILE_CODEC, "png");
        prefs.setValue(PreferenceConstants.IMAGE_TILE_QUALITY, 60);
        prefs.setValue(PreferenceConstants.IMAGE_TILE_COLORS, 256);
        prefs.setValue(PreferenceConstants.IMAGE_TILE_DITHER, true);
        prefs.setValue(PreferenceConstants.IMAGE_TILE_SERPENTINE, false);

        prefs.setValue(PreferenceConstants.PLAYER_RESAMPLE, false);
        prefs.setValue(PreferenceConstants.PLAYER_KEEP_ASPECT_RATIO, true);

        prefs.setValue(PreferenceConstants.SCREEN_INITIAL_MODE,
            Screen.Mode.FOLLOW_MOUSE.name());
        prefs.setValue(PreferenceConstants.SCREEN_MOUSE_AREA_QUALITY,
            VideoSharingPreferenceHelper.ZOOM_LEVELS[0][1]);
        prefs.setValue(PreferenceConstants.SCREEN_MOUSE_AREA_WIDTH, 320);
        prefs.setValue(PreferenceConstants.SCREEN_MOUSE_AREA_HEIGHT, 240);
        prefs.setValue(PreferenceConstants.SCREEN_SHOW_MOUSEPOINTER, true);

        /*
         * Initially 50/50 distribution Roster/Chatpart in saros view
         */
        prefs.setValue(PreferenceConstants.SAROSVIEW_SASH_WEIGHT_LEFT, 1);
        prefs.setValue(PreferenceConstants.SAROSVIEW_SASH_WEIGHT_RIGHT, 1);
    }

    private static interface PreferenceHolderWrapper {
        void setValue(String s, int i);

        void setValue(String s, boolean b);

        void setValue(String s, String s1);
    }

    private static class IEclipsePreferencesWrapper implements
        PreferenceHolderWrapper {
        private IEclipsePreferences preferences;

        private IEclipsePreferencesWrapper(IEclipsePreferences preferences) {
            this.preferences = preferences;
        }

        public void setValue(String s, int i) {
            preferences.putInt(s, i);
        }

        public void setValue(String s, boolean b) {
            preferences.putBoolean(s, b);
        }

        public void setValue(String s, String s1) {
            preferences.put(s, s1);
        }
    }

    private static class IPreferenceStoreWrapper implements
        PreferenceHolderWrapper {
        private IPreferenceStore preferenceStore;

        private IPreferenceStoreWrapper(IPreferenceStore preferenceStore) {
            this.preferenceStore = preferenceStore;
        }

        public void setValue(String s, int i) {
            preferenceStore.setValue(s, i);
        }

        public void setValue(String s, boolean b) {
            preferenceStore.setValue(s, b);
        }

        public void setValue(String s, String s1) {
            preferenceStore.setValue(s, s1);
        }
    }
}
