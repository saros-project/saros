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
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.service.prefs.Preferences;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.editor.colorstorage.UserColorID;
import de.fu_berlin.inf.dpp.feedback.AbstractFeedbackManager;
import de.fu_berlin.inf.dpp.feedback.FeedbackInterval;
import de.fu_berlin.inf.dpp.feedback.FeedbackManager;

/**
 * Class used to initialize default preference values.
 * 
 * @author rdjemili
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

    /*
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer
     */
    private static final Logger LOG = Logger
        .getLogger(PreferenceInitializer.class.getName());

    @Override
    public void initializeDefaultPreferences() {
        LOG.info("initializing preference default values");
        Preferences prefs = new DefaultScope().getNode(Saros.PLUGIN_ID);
        setPreferences(prefs);
    }

    public static void setPreferences(Preferences prefs) {
        setPreferences(new PreferencesWrapper(prefs));
    }

    public static void setPreferences(IPreferenceStore preferenceStore) {
        setPreferences(new IPreferenceStoreWrapper(preferenceStore));
    }

    private static void setPreferences(PreferenceHolderWrapper prefs) {

        prefs.setValue(PreferenceConstants.AUTO_CONNECT, true);
        prefs.setValue(PreferenceConstants.AUTO_PORTMAPPING_DEVICEID, "");
        prefs.setValue(PreferenceConstants.GATEWAYCHECKPERFORMED, false);
        prefs.setValue(PreferenceConstants.SKYPE_USERNAME, "");
        prefs.setValue(PreferenceConstants.DEBUG, false);
        prefs.setValue(PreferenceConstants.FILE_TRANSFER_PORT, 7777);
        prefs.setValue(PreferenceConstants.USE_NEXT_PORTS_FOR_FILE_TRANSFER,
            true);
        prefs.setValue(PreferenceConstants.LOCAL_SOCKS5_PROXY_DISABLED, false);

        prefs.setValue(
            PreferenceConstants.LOCAL_SOCKS5_PROXY_USE_UPNP_EXTERNAL_ADDRESS,
            true);

        prefs.setValue(PreferenceConstants.LOCAL_SOCKS5_PROXY_CANDIDATES, "");
        prefs.setValue(PreferenceConstants.FORCE_FILETRANSFER_BY_CHAT, false);

        prefs.setValue(PreferenceConstants.STUN, "stunserver.org");
        prefs.setValue(PreferenceConstants.STUN_PORT, 0);
        prefs.setValue(PreferenceConstants.CONCURRENT_UNDO, false);
        prefs.setValue(PreferenceConstants.DISABLE_VERSION_CONTROL, false);

        // Advanced Preferences

        prefs.setValue(PreferenceConstants.AUTO_STOP_EMPTY_SESSION,
            MessageDialogWithToggle.PROMPT);

        prefs.setValue(PreferenceConstants.ENABLE_BALLOON_NOTIFICATION, true);

        prefs.setValue(
            PreferenceConstants.CONTACT_SELECTION_FILTER_NON_SAROS_CONTACTS,
            true);

        // Initialize Feedback Preferences
        prefs.setValue(PreferenceConstants.FEEDBACK_SURVEY_DISABLED,
            FeedbackManager.FEEDBACK_ENABLED);
        prefs.setValue(PreferenceConstants.FEEDBACK_SURVEY_INTERVAL,
            FeedbackInterval.DEFAULT.getInterval());
        prefs.setValue(PreferenceConstants.STATISTIC_ALLOW_SUBMISSION,
            AbstractFeedbackManager.UNKNOWN);
        prefs.setValue(PreferenceConstants.ERROR_LOG_ALLOW_SUBMISSION,
            AbstractFeedbackManager.UNKNOWN);
        prefs.setValue(PreferenceConstants.ERROR_LOG_ALLOW_SUBMISSION_FULL,
            AbstractFeedbackManager.FORBID);

        // Communication default settings
        prefs.setValue(PreferenceConstants.USE_IRC_STYLE_CHAT_LAYOUT, false);
        prefs.setValue(PreferenceConstants.CUSTOM_MUC_SERVICE, "");
        prefs.setValue(PreferenceConstants.FORCE_CUSTOM_MUC_SERVICE, true);
        prefs.setValue(PreferenceConstants.SOUND_ENABLED, true);

        prefs.setValue(PreferenceConstants.SOUND_PLAY_EVENT_MESSAGE_SENT, true);
        prefs.setValue(PreferenceConstants.SOUND_PLAY_EVENT_MESSAGE_RECEIVED,
            true);
        prefs.setValue(PreferenceConstants.SOUND_PLAY_EVENT_CONTACT_ONLINE,
            true);
        prefs.setValue(PreferenceConstants.SOUND_PLAY_EVENT_CONTACT_OFFLINE,
            true);

        // consolesharing
        prefs.setValue(PreferenceConstants.CONSOLESHARING_ENABLED, false);

        /*
         * Initially 50/50 distribution Roster/Chatpart in saros view
         */
        prefs.setValue(PreferenceConstants.SAROSVIEW_SASH_WEIGHT_LEFT, 1);
        prefs.setValue(PreferenceConstants.SAROSVIEW_SASH_WEIGHT_RIGHT, 1);

        prefs.setValue(PreferenceConstants.FAVORITE_SESSION_COLOR_ID,
            UserColorID.UNKNOWN);

        // Hack for MARCH 2013 release

        prefs.setValue("FAVORITE_COLOR_ID_HACK_CREATE_RANDOM_COLOR", true);

        /*
         * Editor stuff
         */

        prefs.setValue(PreferenceConstants.SHOW_CONTRIBUTION_ANNOTATIONS,
            "true");
        prefs.setValue(PreferenceConstants.SHOW_SELECTIONFILLUP_ANNOTATIONS,
            "false"); // set to "false" because of poor performance
    }

    private static interface PreferenceHolderWrapper {
        void setValue(String s, int i);

        void setValue(String s, boolean b);

        void setValue(String s, String s1);
    }

    private static class PreferencesWrapper implements PreferenceHolderWrapper {
        private Preferences preferences;

        private PreferencesWrapper(Preferences preferences) {
            this.preferences = preferences;
        }

        @Override
        public void setValue(String s, int i) {
            preferences.putInt(s, i);
        }

        @Override
        public void setValue(String s, boolean b) {
            preferences.putBoolean(s, b);
        }

        @Override
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

        @Override
        public void setValue(String s, int i) {
            preferenceStore.setValue(s, i);
        }

        @Override
        public void setValue(String s, boolean b) {
            preferenceStore.setValue(s, b);
        }

        @Override
        public void setValue(String s, String s1) {
            preferenceStore.setValue(s, s1);
        }
    }
}
