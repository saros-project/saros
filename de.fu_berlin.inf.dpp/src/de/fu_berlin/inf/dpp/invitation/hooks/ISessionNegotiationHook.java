package de.fu_berlin.inf.dpp.invitation.hooks;

import java.util.Map;

import de.fu_berlin.inf.dpp.invitation.IncomingSessionNegotiation;
import de.fu_berlin.inf.dpp.invitation.OutgoingSessionNegotiation;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;

/**
 * A SessionNegotiationHook is a component that whishes to take part in the
 * SessionNegotiation. On the client side, this might be in the form of whishes
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
     * 
     * The identifier is used to match the two parts of the hook (on client and
     * host side). Therefore, changing the identifier of a hook breaks the
     * compatibility with older Saros versions.
     * 
     * @return A unique string identifying the hook.
     */
    public String getIdentifier();

    /**
     * Receive the client's preferences for later consideration.
     * 
     * During the invitation this method will be called on the <b>client</b>
     * side (see {@link IncomingSessionNegotiation}). The client may use this
     * oppurtunity to tell the host (inviter) his preferences concerning the
     * session parameters.
     * 
     * @return The settings in form of [Key, Value] pairs. If <code>null</code>,
     *         the settings won't be transferred to the host.
     */
    public Map<String, String> tellClientPreferences();

    /**
     * Consider the client's preferences on the host side.
     * 
     * This method will be called by the <b>host</b> (during the
     * {@link OutgoingSessionNegotiation}) to determine the session settings.
     * Therefore, the host might consider the preferences of the client.
     * 
     * @param input
     *            The preferences the client sent during his
     *            {@link IncomingSessionNegotiation} (i.e. the return value of
     *            {@link ISessionNegotiationHook#tellClientPreferences()}).
     * @return The settings determined by the host which -- if not null -- will
     *         be sent back to the client. It's up to the specific hook to which
     *         extent the host considers the wishes of the client.
     */
    public Map<String, String> considerClientPreferences(
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
     *            The parameters concerning the hook at hand which were
     *            determined by the host during his
     *            {@link OutgoingSessionNegotiation}.
     */
    public void applyActualParameters(Map<String, String> settings);
}