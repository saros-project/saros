package saros.session;

import java.util.Collections;
import java.util.Map;
import org.apache.log4j.Logger;
import saros.negotiation.TransferType;
import saros.negotiation.hooks.ISessionNegotiationHook;
import saros.negotiation.hooks.SessionNegotiationHookManager;
import saros.net.xmpp.JID;
import saros.preferences.IPreferenceStore;
import saros.preferences.Preferences;

/**
 * Hooks for negotiating the used {@link TransferType} to determine a strategy used between two
 * {@link User}s to transfer files during a project negotiation.
 *
 * <p>Host and Client tell a preference, the host decides if they are the same and use it. Otherwise
 * defaults to Archive. This approach is good enough and works best for two supported types, like
 * now {@link TransferType#ARCHIVE} and {@link TransferType#INSTANT}.
 */
public class ProjectNegotiationTypeHook implements ISessionNegotiationHook {
  private static final String HOOK_IDENTIFIER = "projectNegotiationTypeHook";
  private static final String KEY_PREFERRED_TYPE = "preferredProjectNegotiationType";

  public static final String KEY_TYPE = "projectNegotiationType";

  private static final String TYPE_ARCHIVE = TransferType.ARCHIVE.name();
  private static final String TYPE_INSTANT = TransferType.INSTANT.name();

  private static final Logger LOG = Logger.getLogger(ProjectNegotiationTypeHook.class);

  private Preferences localPref;

  public ProjectNegotiationTypeHook(
      SessionNegotiationHookManager hookManager, Preferences localPref) {
    this.localPref = localPref;
    hookManager.addHook(this);
  }

  @Override
  public String getIdentifier() {
    return HOOK_IDENTIFIER;
  }

  @Override
  public void setInitialHostPreferences(IPreferenceStore hostPreferences) {
    // NOP
  }

  @Override
  public Map<String, String> tellClientPreferences() {
    return getLocalPreference();
  }

  @Override
  public Map<String, String> considerClientPreferences(JID client, Map<String, String> input) {
    if (input == null || !input.containsKey(KEY_PREFERRED_TYPE)) {
      LOG.warn(
          "The client did not indicate any transfer type "
              + "preferences. This could be an indication for a version "
              + "mismatch.");
      return null;
    }

    /* if both prefer the same type, set it */
    String inputType = input.get(KEY_PREFERRED_TYPE);
    if (inputType.equals(getLocalPreference().get(KEY_PREFERRED_TYPE))) {
      return Collections.singletonMap(KEY_TYPE, inputType);
    }

    /* otherwise Archive should currently be the field-tested fallback */
    return Collections.singletonMap(KEY_TYPE, TYPE_ARCHIVE);
  }

  @Override
  public void applyActualParameters(
      Map<String, String> input,
      IPreferenceStore hostPreferences,
      IPreferenceStore clientPreferences) {
    if (input == null || !input.containsKey(KEY_TYPE)) {
      LOG.warn(
          "The host did not set any parameters. This may be caused"
              + "if the client did not indicate any transfer type "
              + "preferences to begin with."
              + "This could also be an indication for a version "
              + "mismatch.");
      return;
    }

    TransferType type;
    try {
      type = TransferType.valueOf(input.get(KEY_TYPE));
    } catch (IllegalArgumentException e) {
      LOG.warn(
          "The client send a unknown transfer type: '"
              + input.get(KEY_TYPE)
              + "'! This could be an indication for a version mismatch.");
      return;
    }

    hostPreferences.setValue(KEY_TYPE, type.name());

    clientPreferences.setValue(KEY_TYPE, type.name());
  }

  private Map<String, String> getLocalPreference() {
    if (localPref != null && localPref.isInstantSessionStartPreferred()) {
      return Collections.singletonMap(KEY_PREFERRED_TYPE, TYPE_INSTANT);
    }

    return Collections.singletonMap(KEY_PREFERRED_TYPE, TYPE_ARCHIVE);
  }
}
