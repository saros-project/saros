package de.fu_berlin.inf.dpp.project.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.fu_berlin.inf.dpp.negotiation.hooks.ISessionNegotiationHook;
import de.fu_berlin.inf.dpp.negotiation.hooks.SessionNegotiationHookManager;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.preferences.Preferences;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.User;

public class NicknameNegotiationHook implements ISessionNegotiationHook {
    private static final String HOOK_IDENTIFIER = "nicknameManagement";
    // This high visibility is not needed for good. Currently there is a
    // nickname
    // related HACK in the SessionNegotiation (out/in) that relies on these
    // constants.

    // FIXME this is HACK number 2, the hooking mechanism is currently not very
    // well designed

    public static final String KEY_CLIENT_NICKNAME = "clientNickname";
    public static final String KEY_HOST_NICKNAME = "hostNickname";

    private final Preferences preferences;
    private final ISarosSessionManager sessionManager;

    public NicknameNegotiationHook(Preferences preferences,
        SessionNegotiationHookManager hooks, ISarosSessionManager sessionManager) {
        this.preferences = preferences;
        this.sessionManager = sessionManager;
        hooks.addHook(this);
    }

    @Override
    public Map<String, String> tellClientPreferences() {
        return Collections.singletonMap(KEY_CLIENT_NICKNAME,
            preferences.getSessionNickname());
    }

    @Override
    public Map<String, String> considerClientPreferences(final JID client,
        final Map<String, String> input) {

        final Map<String, String> result = new HashMap<String, String>();

        final ISarosSession session = sessionManager.getSarosSession();

        if (session == null)
            return result;

        /*
         * FIXME if two user joining at the same time the nickname will be the
         * same
         */
        final Set<String> occupiedNicknames = new HashSet<String>();

        for (final User user : session.getUsers())
            occupiedNicknames.add(user.getNickname());

        String nickname = input == null ? null : input.get(KEY_CLIENT_NICKNAME);

        if (nickname == null || nickname.isEmpty())
            nickname = client.getBareJID().toString();

        int suffix = 2;

        String nicknameToUse = nickname;

        while (occupiedNicknames.contains(nicknameToUse))
            nicknameToUse = nickname + " (" + (suffix++) + ")";

        assert session.getLocalUser().equals(session.getHost());

        result.put(KEY_HOST_NICKNAME, session.getLocalUser().getNickname());
        result.put(KEY_CLIENT_NICKNAME, nicknameToUse);

        return result;
    }

    @Override
    public void applyActualParameters(Map<String, String> settings) {
        // TODO Implement the application of the returned nickname settings.
        // This
        // is currently done with a HACK in IncomingSessionNegotiation (see
        // method initializeSession())
    }

    @Override
    public String getIdentifier() {
        return HOOK_IDENTIFIER;
    }

}
