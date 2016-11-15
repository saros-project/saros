package de.fu_berlin.inf.dpp.session;

import java.util.HashMap;
import java.util.Map;

import de.fu_berlin.inf.dpp.negotiation.hooks.ISessionNegotiationHook;
import de.fu_berlin.inf.dpp.negotiation.hooks.SessionNegotiationHookManager;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.preferences.Preferences;

public class ColorNegotiationHook implements ISessionNegotiationHook {
    private static final String HOOK_IDENTIFIER = "colorManagement";
    // This high visibility is not needed for good. Currently there is a color
    // related HACK in the SessionNegotiation that relies on these constants.
    public static final String KEY_CLIENT_COLOR = "clientColor";
    public static final String KEY_CLIENT_FAV_COLOR = "clientFavoriteColor";
    public static final String KEY_HOST_COLOR = "hostColor";
    public static final String KEY_HOST_FAV_COLOR = "hostFavoriteColor";

    private Preferences preferences;
    private ISarosSessionManager sessionManager;

    public ColorNegotiationHook(Preferences preferences,
        SessionNegotiationHookManager hooks, ISarosSessionManager sessionManager) {
        this.preferences = preferences;
        this.sessionManager = sessionManager;
        hooks.addHook(this);
    }

    @Override
    public Map<String, String> tellClientPreferences() {
        String favoriteColor = Integer.toString(preferences
            .getFavoriteColorID());

        Map<String, String> colorSettings = new HashMap<String, String>();
        colorSettings.put(KEY_CLIENT_COLOR, favoriteColor);
        colorSettings.put(KEY_CLIENT_FAV_COLOR, favoriteColor);

        return colorSettings;
    }

    @Override
    public Map<String, String> considerClientPreferences(JID client,
        Map<String, String> input) {

        if (input == null)
            return null;

        String hostColor = Integer.toString(sessionManager.getSession()
            .getLocalUser().getColorID());

        String hostFavoriteColor = Integer.toString(preferences
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
        // is currently done with a HACK in IncomingSessionNegotiation (see
        // method initializeSession())
    }

    @Override
    public String getIdentifier() {
        return HOOK_IDENTIFIER;
    }

}