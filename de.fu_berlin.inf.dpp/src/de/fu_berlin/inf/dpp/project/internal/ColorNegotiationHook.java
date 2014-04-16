package de.fu_berlin.inf.dpp.project.internal;

import java.util.HashMap;
import java.util.Map;

import de.fu_berlin.inf.dpp.invitation.hooks.ISessionNegotiationHook;
import de.fu_berlin.inf.dpp.invitation.hooks.SessionNegotiationHookManager;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;

public class ColorNegotiationHook implements ISessionNegotiationHook {
    private static final String HOOK_IDENTIFIER = "colorManagement";
    // This high visibility is not needed for good. Currently there is a color
    // related HACK in the SessionNegotation that relies on these constants.
    public static final String KEY_CLIENT_COLOR = "clientColor";
    public static final String KEY_CLIENT_FAV_COLOR = "clientFavoriteColor";
    public static final String KEY_HOST_COLOR = "hostColor";
    public static final String KEY_HOST_FAV_COLOR = "hostFavoriteColor";

    private PreferenceUtils preferenceUtils;
    private ISarosSessionManager sessionManager;

    public ColorNegotiationHook(PreferenceUtils utils,
        SessionNegotiationHookManager hooks, ISarosSessionManager sessionManager) {
        this.preferenceUtils = utils;
        this.sessionManager = sessionManager;
        hooks.addHook(this);
    }

    @Override
    public Map<String, String> tellClientPreferences() {
        String favoriteColor = Integer.toString(preferenceUtils
            .getFavoriteColorID());

        Map<String, String> colorSettings = new HashMap<String, String>();
        colorSettings.put(KEY_CLIENT_COLOR, favoriteColor);
        colorSettings.put(KEY_CLIENT_FAV_COLOR, favoriteColor);

        return colorSettings;
    }

    @Override
    public Map<String, String> considerClientPreferences(JID client,
        Map<String, String> input) {
        String hostColor = Integer.toString(sessionManager.getSarosSession()
            .getLocalUser().getColorID());
        String hostFavoriteColor = Integer.toString(preferenceUtils
            .getFavoriteColorID());

        Map<String, String> defined = new HashMap<String, String>();
        defined.put(KEY_CLIENT_COLOR, input.get(KEY_CLIENT_COLOR));
        defined.put(KEY_CLIENT_FAV_COLOR, input.get(KEY_CLIENT_FAV_COLOR));
        defined.put(KEY_HOST_COLOR, hostColor);
        defined.put(KEY_HOST_FAV_COLOR, hostFavoriteColor);

        return defined;
    }

    @Override
    public void applyActualParameters(Map<String, String> settings) {
        // TODO Implement the application of the returned color settings. This
        // is currently done with a HACK in IncomingSessionNegotation (see
        // method initializeSession())
    }

    @Override
    public String getIdentifier() {
        return HOOK_IDENTIFIER;
    }

}