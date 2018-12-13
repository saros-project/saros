package de.fu_berlin.inf.dpp.negotiation.hooks;

import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.preferences.IPreferenceStore;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractPropertyHook implements ISessionNegotiationHook {
  protected abstract Object getValue();

  protected abstract Object fromString(String value);

  public static String keyFromIdentifier(String identifier) {
    return identifier + "Property";
  }

  private String getKey() {
    return keyFromIdentifier(getIdentifier());
  }

  private String getServerKey() {
    return "server" + getKey();
  }

  private String getClientKey() {
    return "client" + getKey();
  }

  @Override
  public final void setInitialHostPreferences(IPreferenceStore hostPreferences) {
    Object value = getValue();
    if (value instanceof Boolean) {
      hostPreferences.setValue(getKey(), (Boolean) value);
    } else if (value instanceof Integer) {
      hostPreferences.setValue(getKey(), (Integer) value);
    } else if (value instanceof Long) {
      hostPreferences.setValue(getKey(), (Long) value);
    } else if (value instanceof String) {
      hostPreferences.setValue(getKey(), (String) value);
    } else {
      throw new IllegalArgumentException(
          "PropertyHook values must be either bool,int,long or String");
    }
  }

  @Override
  public final Map<String, String> tellClientPreferences() {
    Map<String, String> clientPreferences = new HashMap<>();
    clientPreferences.put(getClientKey(), getValue().toString());
    return clientPreferences;
  }

  @Override
  public final Map<String, String> considerClientPreferences(
      JID client, Map<String, String> input) {
    Map<String, String> defined = new HashMap<>();
    defined.put(getClientKey(), input.get(getClientKey()));
    defined.put(getServerKey(), getValue().toString());
    return defined;
  }

  @Override
  public final void applyActualParameters(
      Map<String, String> input,
      IPreferenceStore hostPreferences,
      IPreferenceStore clientPreferences) {
    Object clientValue = fromString(input.get(getClientKey()));
    Object serverValue = fromString(input.get(getServerKey()));

    assert clientValue.getClass() != serverValue.getClass();

    if (clientValue instanceof Boolean) {
      clientPreferences.setValue(getKey(), (Boolean) clientValue);
      hostPreferences.setValue(getKey(), (Boolean) serverValue);
    } else if (clientValue instanceof Integer) {
      clientPreferences.setValue(getKey(), (Integer) clientValue);
      hostPreferences.setValue(getKey(), (Integer) serverValue);
    } else if (clientValue instanceof Long) {
      clientPreferences.setValue(getKey(), (Long) clientValue);
      hostPreferences.setValue(getKey(), (Long) serverValue);
    } else if (clientValue instanceof String) {
      clientPreferences.setValue(getKey(), (String) clientValue);
      hostPreferences.setValue(getKey(), (String) serverValue);
    } else {
      throw new IllegalArgumentException(
          "PropertyHook values must be either bool,int,long or String");
    }
  }
}
