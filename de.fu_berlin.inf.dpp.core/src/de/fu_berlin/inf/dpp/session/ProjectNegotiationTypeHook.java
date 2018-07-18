package de.fu_berlin.inf.dpp.session;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.negotiation.TransferType;
import de.fu_berlin.inf.dpp.negotiation.hooks.ISessionNegotiationHook;
import de.fu_berlin.inf.dpp.negotiation.hooks.SessionNegotiationHookManager;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.preferences.IPreferenceStore;

/**
 * Hooks for negotiating the used {@link TransferType} to determine a strategy
 * used between two {@link User}s to transfer files during a project
 * negotiation.
 * 
 * Currently only {@link #TYPE_ARCHIVE} exists and is always selected.
 */
public class ProjectNegotiationTypeHook implements ISessionNegotiationHook {
    private static final String HOOK_IDENTIFIER = "projectNegotiationTypeHook";
    private static final String KEY_PREFERRED_TYPE = "preferredProjectNegotiationType";

    public static final String KEY_TYPE = "projectNegotiationType";

    private static final String TYPE_ARCHIVE = TransferType.ARCHIVE.name();

    private static final Logger LOG = Logger
        .getLogger(ProjectNegotiationTypeHook.class);

    public ProjectNegotiationTypeHook(SessionNegotiationHookManager hookManager) {
        hookManager.addHook(this);
    }

    @Override
    public String getIdentifier() {
        return HOOK_IDENTIFIER;
    }

    @Override
    public Map<String, String> tellHostPreferences() {
        Map<String, String> hostPreferences = new HashMap<String, String>();
        // Currently the host does have a general preference, that is used
        // if the client indicates support as well. Otherwise a fallback shall
        // be used.
        // In future multiple TransferTypes might require the client to send
        // a list of supported types instead of one preference.
        hostPreferences.put(KEY_PREFERRED_TYPE, TYPE_ARCHIVE);
        return hostPreferences;
    }

    @Override
    public Map<String, String> tellClientPreferences() {
        Map<String, String> clientPreferences = new HashMap<String, String>();
        // The clients preferences may enable more advanced transfer types,
        // if the host agrees. The host decides upon the final transfer type
        // and might choose a generally supported fallback instead.
        clientPreferences.put(KEY_PREFERRED_TYPE, TYPE_ARCHIVE);
        return clientPreferences;
    }

    @Override
    public Map<String, String> considerClientPreferences(JID client,
        Map<String, String> input) {
        if (input == null) {
            LOG.warn("The client did not indicate any transfer type "
                + "preferences. This could be an indication for a version "
                + "mismatch.");
            return null;
        }

        Map<String, String> finalParameters = new HashMap<String, String>();

        // A new transfer type should be selected here,
        // if the client indicates support/preference and the host does as well.
        // Archive should currently always be the field-tested fallback.

        if (input.get(KEY_PREFERRED_TYPE).equals(
            tellHostPreferences().get(KEY_PREFERRED_TYPE))) {
            finalParameters.put(KEY_TYPE, input.get(KEY_PREFERRED_TYPE));
        } else {
            finalParameters.put(KEY_TYPE, TYPE_ARCHIVE);
        }

        return finalParameters;
    }

    @Override
    public void applyActualParameters(Map<String, String> input,
        IPreferenceStore hostPreferences, IPreferenceStore clientPreferences) {
        if (input == null) {
            LOG.warn("The host did not set any parameters. This may be caused"
                + "if the client did not indicate any transfer type "
                + "preferences to begin with."
                + "This could also be an indication for a version "
                + "mismatch.");
            return;
        }

        TransferType type = TransferType.valueOf(input.get(KEY_TYPE));
        hostPreferences.setValue(KEY_TYPE, type.name());
        if (clientPreferences != null) {
            clientPreferences.setValue(KEY_TYPE, type.name());
        }
    }
}