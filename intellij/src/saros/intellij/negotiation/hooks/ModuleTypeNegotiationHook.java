package saros.intellij.negotiation.hooks;

import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import saros.filesystem.IProject;
import saros.intellij.filesystem.IntelliJProjectImpl;
import saros.intellij.ui.util.NotificationPanel;
import saros.negotiation.hooks.ISessionNegotiationHook;
import saros.negotiation.hooks.SessionNegotiationHookManager;
import saros.net.xmpp.JID;
import saros.preferences.IPreferenceStore;
import saros.session.ISarosSessionManager;

/**
 * Negotiation Hook to exchange information about possible module types as well as the types of the
 * shared modules.
 *
 * <p>This information is used during session negotiation to provide a warning in case the type of a
 * shared module in not known to all clients. This warning is shown as a notification to the host
 * and a dialog window to the affected client(s).
 *
 * <p>
 */
public class ModuleTypeNegotiationHook implements ISessionNegotiationHook {
  private static final String HOOK_IDENTIFIER = "moduleTypeNegotiation";

  private static final String KEY_AVAILABLE_TYPES = "availableModuleTypes";
  private static final String KEY_TYPE_MAPPINGS = "typeMappings";

  private static final Logger log = Logger.getLogger(ModuleTypeNegotiationHook.class);

  private final ISarosSessionManager sessionManager;

  private final ModuleTypeManager moduleTypeManager;

  /**
   * Creates a <code>ModuleTypeNegotiationHook</code> object and adds it to the {@link
   * SessionNegotiationHookManager}.
   *
   * <p><b>Note:</b> The <code>ModuleTypeNegotiationHook</code> should only be instantiated by the
   * PicoContainer as part of the saros plugin context.
   *
   * @param hookManager the <code>SessionNegotiationHookManager</code>
   * @param sessionManager the <code>SessionManger</code>
   * @see saros.intellij.context.SarosIntellijContextFactory
   */
  public ModuleTypeNegotiationHook(
      SessionNegotiationHookManager hookManager, ISarosSessionManager sessionManager) {
    this.sessionManager = sessionManager;

    this.moduleTypeManager = ModuleTypeManager.getInstance();

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
    Map<String, String> clientModuleTypes = new HashMap<>();

    StringBuilder stringBuilder = new StringBuilder();

    for (ModuleType type : moduleTypeManager.getRegisteredTypes()) {
      stringBuilder.append(String.format("%s\t", type.getId()));
    }

    clientModuleTypes.put(KEY_AVAILABLE_TYPES, stringBuilder.toString());

    return clientModuleTypes;
  }

  @Override
  public Map<String, String> considerClientPreferences(JID client, Map<String, String> input) {
    Collection<String> clientModuleTypes;

    if (input == null) {
      clientModuleTypes = new ArrayList<>();
      log.warn(
          "The client did not indicate any module type "
              + "preferences. This could be an indication for a version "
              + "mismatch.");
    } else {
      clientModuleTypes = Arrays.asList(input.get(KEY_AVAILABLE_TYPES).split("\t"));
    }

    Map<String, String> sharedParameters = new HashMap<>();

    StringBuilder stringBuilder = new StringBuilder();

    for (final IProject project : sessionManager.getSession().getProjects()) {

      IntelliJProjectImpl intelliJProject = project.adaptTo(IntelliJProjectImpl.class);

      String moduleType = ModuleType.get(intelliJProject.getModule()).getId();

      if (!clientModuleTypes.isEmpty() && !clientModuleTypes.contains(moduleType)) {

        String warningMessage =
            "The module type \""
                + moduleType
                + "\" of the module \""
                + intelliJProject.getName()
                + "\" is not known to the client \""
                + client
                + "\". This might lead to an unexpected behavior of Saros.";

        log.warn(warningMessage);

        NotificationPanel.showWarning(warningMessage, "Unknown module type");
      }

      stringBuilder.append(String.format("%s:%s\t", intelliJProject.getName(), moduleType));
    }

    sharedParameters.put(KEY_TYPE_MAPPINGS, stringBuilder.toString());

    return sharedParameters;
  }

  @Override
  public void applyActualParameters(
      Map<String, String> input,
      IPreferenceStore hostPreferences,
      IPreferenceStore clientPreferences) {

    if (input == null) {
      log.warn(
          "The client did not indicate any module type "
              + "preferences. This could be an indication for a version "
              + "mismatch.");
      return;
    }

    List<ModuleType> availableTypes = Arrays.asList(moduleTypeManager.getRegisteredTypes());

    StringBuilder stringBuilder = new StringBuilder();

    for (String mappingEntry : input.get(KEY_TYPE_MAPPINGS).split("\t")) {

      String[] separatedEntry = mappingEntry.split(":");
      String key = separatedEntry[0];
      String value = separatedEntry[1];

      ModuleType moduleType = moduleTypeManager.findByID(value);

      if (!availableTypes.contains(moduleType)) {
        log.warn(
            "The module type \""
                + value
                + "\" of the module "
                + key
                + " shared by the host is not known to the local "
                + "IntelliJ instance. This might lead to unexpected "
                + "behavior of Saros");

        stringBuilder.append(String.format("%s : %s%n", key, value));
      }
    }

    if (stringBuilder.length() > 0) {
      stringBuilder.insert(
          0,
          "Some of the shared modules have types that"
              + " are not known to the local IntelliJ instance.\n"
              + "This might lead to an unexpected behavior of Saros.\n\n"
              + "The following modules are affected (name : type):\n");

      stringBuilder.deleteCharAt(stringBuilder.length() - 1);

      NotificationPanel.showWarning(stringBuilder.toString(), "Unknown Module Type(s)");
    }
  }
}
