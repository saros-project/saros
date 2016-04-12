package de.fu_berlin.inf.dpp.negotiation.hooks;

import java.util.Map;

import de.fu_berlin.inf.dpp.negotiation.IncomingSessionNegotiation;
import de.fu_berlin.inf.dpp.negotiation.OutgoingSessionNegotiation;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.session.SarosSessionManager;

/**
 * A SessionNegotiationHook is a component that wishes to take part in the
 * SessionNegotiation. On the client side, this might be in the form of wishes
 * (during the {@link IncomingSessionNegotiation}), which may then be considered
 * by the host (during the {@link OutgoingSessionNegotiation}). The settings
 * determined by the host need to be applied on the client side.
 * 
 * Hooks are maintained by the {@link SessionNegotiationHookManager}. Hooks may
 * not rely on other hooks, i.e. there are no warranties concerning the order of
 * execution.
 */
public interface ISessionNegotiationHook {
    /**
     * Retrieves the hook's identifier.
     * <p>
     * The identifier is used to match the two parts of the hook (on client and
     * host side). Therefore, changing the identifier of a hook breaks the
     * compatibility with older Saros versions.
     * 
     * @return A unique string identifying the hook.
     */
    public String getIdentifier();

    /**
     * Receive the client's preferences for later consideration.
     * <p>
     * During the invitation this method will be called on the <b>client</b>
     * side (see {@link IncomingSessionNegotiation}). The client may use this
     * opportunity to tell the host (inviter) his preferences concerning the
     * session parameters.
     * 
     * @return The settings in form of [Key, Value] pairs. If <code>null</code>,
     *         the settings won't be transferred to the host.
     */
    public Map<String, String> tellClientPreferences();

    /**
     * Consider the client's preferences on the host side.
     * <p>
     * This method will be called by the <b>host</b> (during the
     * {@link OutgoingSessionNegotiation}) to determine the session settings.
     * Therefore, the host might consider the preferences of the client.
     * 
     * @param client
     *            The JID of the user the <code>input</code> comes from (just in
     *            case the host needs to keep track of who preferred what)
     * @param input
     *            The preferences the client sent during his
     *            {@link IncomingSessionNegotiation} (i.e. the return value of
     *            {@link #tellClientPreferences()}). Might be <code>null</code>,
     *            if the client has no counterpart for this hook.
     * 
     * @return The settings determined by the host which -- if not
     *         <code>null</code> -- will be sent back to the client. It's up to
     *         the specific hook to which extent the host considers the wishes
     *         of the client.
     */
    public Map<String, String> considerClientPreferences(JID client,
        Map<String, String> input);

    /**
     * Duty of the client: Apply the parameters defined by the host.
     * 
     * This method will be called on the <b>client</b>'s side upon reception of
     * the actual session parameters determined by the host. The hook itself is
     * responsible for accessing and modifying the according components (e.g.
     * the {@link SarosSessionManager}). This method will be called right before
     * the Session is created on the client side (via
     * <code>SarosSessionManager.joinSession()</code>) and may not rely on the
     * effects of other hooks.
     * 
     * @param settings
     *            The parameters concerning the hook at hand, which were
     *            determined by the host during his
     *            {@link OutgoingSessionNegotiation} (i.e. the return value of
     *            {@link #considerClientPreferences(JID, Map)} ). Might be
     *            <code>null</code>, if the host has no counterpart for this
     *            hook.
     */
    public void applyActualParameters(Map<String, String> settings);
}