/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universitaet Berlin - Fachbereich Mathematik und Informatik - 2006
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
    protected static Logger log = Logger.getLogger(PreferenceInitializer.class
        .getName());

    @Override
    public void initializeDefaultPreferences() {
        IEclipsePreferences prefs = new DefaultScope().getNode(Saros.SAROS);

        prefs.putBoolean(PreferenceConstants.ENCRYPT_ACCOUNT, false);
        prefs.putBoolean(PreferenceConstants.AUTO_CONNECT, false);
        prefs.putBoolean(PreferenceConstants.AUTO_FOLLOW_MODE, false);
        prefs.put(PreferenceConstants.SKYPE_USERNAME, "");
        prefs.putBoolean(PreferenceConstants.DEBUG, false);
        prefs.putInt(PreferenceConstants.FILE_TRANSFER_PORT, 7777);
        prefs.putBoolean(PreferenceConstants.USE_NEXT_PORTS_FOR_FILE_TRANSFER,
            true);
        prefs
            .putBoolean(PreferenceConstants.LOCAL_SOCKS5_PROXY_DISABLED, false);
        prefs.putBoolean(PreferenceConstants.FORCE_FILETRANSFER_BY_CHAT, false);
        prefs.putInt(PreferenceConstants.CHATFILETRANSFER_CHUNKSIZE, 50000);
        prefs.put(PreferenceConstants.STUN, "stunserver.org");
        prefs.putInt(PreferenceConstants.STUN_PORT, 3478);
        prefs.putInt(PreferenceConstants.MILLIS_UPDATE, 300);
        prefs.putBoolean(PreferenceConstants.AUTO_ACCEPT_INVITATION, false);
        prefs.putBoolean(PreferenceConstants.CONCURRENT_UNDO, false);
        prefs.putBoolean(PreferenceConstants.PING_PONG, false);
        prefs.putBoolean(PreferenceConstants.DISABLE_VERSION_CONTROL, false);

        // InvitationDialog
        prefs.putBoolean(PreferenceConstants.AUTO_CLOSE_DIALOG, true);
        prefs.putBoolean(PreferenceConstants.SKIP_SYNC_SELECTABLE, false);

        // its a new workspace per default, is set to false after first start in
        // earlyStartup()
        prefs.putBoolean(PreferenceConstants.NEW_WORKSPACE, true);

        // Initialize Feedback Preferences
        prefs.putInt(PreferenceConstants.FEEDBACK_SURVEY_DISABLED,
            FeedbackManager.FEEDBACK_ENABLED);
        prefs.putInt(PreferenceConstants.FEEDBACK_SURVEY_INTERVAL, 5);
        prefs.putInt(PreferenceConstants.STATISTIC_ALLOW_SUBMISSION,
            AbstractFeedbackManager.UNKNOWN);
        prefs.putInt(PreferenceConstants.ERROR_LOG_ALLOW_SUBMISSION,
            AbstractFeedbackManager.UNKNOWN);
        prefs.putInt(PreferenceConstants.ERROR_LOG_ALLOW_SUBMISSION_FULL,
            AbstractFeedbackManager.FORBID);

        // Communication default settings
        prefs.put(PreferenceConstants.CHATSERVER, "conference.jabber.ccc.de");
        prefs.putBoolean(PreferenceConstants.BEEP_UPON_IM, true);
        prefs.putBoolean(PreferenceConstants.AUDIO_VBR, true);
        prefs.putBoolean(PreferenceConstants.AUDIO_ENABLE_DTX, true);
        prefs.put(PreferenceConstants.AUDIO_SAMPLERATE, "44100");
        prefs.put(PreferenceConstants.AUDIO_QUALITY_LEVEL, "8");

        // videosharing

        prefs.putInt(PreferenceConstants.ENCODING_VIDEO_FRAMERATE, 10);
        prefs.put(PreferenceConstants.ENCODING_VIDEO_RESOLUTION,
            VideoSharingPreferenceHelper.RESOLUTIONS[2][1]);
        prefs.putInt(PreferenceConstants.ENCODING_VIDEO_WIDTH, 320);
        prefs.putInt(PreferenceConstants.ENCODING_VIDEO_HEIGHT, 240);
        prefs.putInt(PreferenceConstants.ENCODING_MAX_BITRATE, 512000);
        prefs.put(PreferenceConstants.ENCODING_CODEC,
            VideoSharing.Codec.XUGGLER.name());

        prefs.put(PreferenceConstants.XUGGLER_CONTAINER_FORMAT, "flv");
        prefs.put(PreferenceConstants.XUGGLER_CODEC, "libx264");
        prefs.putBoolean(PreferenceConstants.XUGGLER_USE_VBV, false);

        prefs.put(PreferenceConstants.IMAGE_TILE_CODEC, "png");
        prefs.putInt(PreferenceConstants.IMAGE_TILE_QUALITY, 60);
        prefs.putInt(PreferenceConstants.IMAGE_TILE_COLORS, 256);
        prefs.putBoolean(PreferenceConstants.IMAGE_TILE_DITHER, true);
        prefs.putBoolean(PreferenceConstants.IMAGE_TILE_SERPENTINE, false);

        prefs.putBoolean(PreferenceConstants.PLAYER_RESAMPLE, false);
        prefs.putBoolean(PreferenceConstants.PLAYER_KEEP_ASPECT_RATIO, true);

        prefs.put(PreferenceConstants.SCREEN_INITIAL_MODE,
            Screen.Mode.FOLLOW_MOUSE.name());
        prefs.put(PreferenceConstants.SCREEN_MOUSE_AREA_QUALITY,
            VideoSharingPreferenceHelper.ZOOM_LEVELS[0][1]);
        prefs.putInt(PreferenceConstants.SCREEN_MOUSE_AREA_WIDTH, 320);
        prefs.putInt(PreferenceConstants.SCREEN_MOUSE_AREA_HEIGHT, 240);
        prefs.putBoolean(PreferenceConstants.SCREEN_SHOW_MOUSEPOINTER, true);
    }
}
