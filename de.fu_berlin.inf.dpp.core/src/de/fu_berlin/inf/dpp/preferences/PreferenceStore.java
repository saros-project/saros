package de.fu_berlin.inf.dpp.preferences;

import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.log4j.Logger;

/**
 * Default implementation of the {@link IPreferenceStore} interface.
 *
 * <p>It offers all functionality specified by the interface but does not support the storage of the
 * preferences, i.e this implementation purely memory based.
 *
 * <p>Clients may subclass this implementation to redirect the storage of the preferences to a
 * persistent store for example.
 *
 * <p>In order to correctly subclass this implementation in regards of redirection client have to
 * override the {@linkplain #setPreference} <b>AND</b> {@linkplain #getPreference} methods.
 */
public class PreferenceStore implements IPreferenceStore {

  private static final Logger LOG = Logger.getLogger(PreferenceStore.class);

  /** The default-default value for boolean preferences (<code>false</code>). */
  public static final boolean DEFAULT_BOOLEAN = false;

  /** The default-default value for integer preferences (<code>0</code>). */
  public static final int DEFAULT_INT = 0;

  /** The default-default value for long preferences (<code>0</code>). */
  public static final long DEFAULT_LONG = 0L;

  /** The default-default value for string preferences (<code>""</code>). */
  public static final String DEFAULT_STRING = "";

  /**
   * Storage for common preferences. Subclasses may ignore this storage and use their own instead.
   */
  protected final Properties properties;

  /**
   * Contains the currently registered listeners. Subclasses are <b>not</b> allowed to alter the
   * content.
   */
  protected final CopyOnWriteArrayList<IPreferenceChangeListener> changeListeners =
      new CopyOnWriteArrayList<IPreferenceChangeListener>();

  private final Properties defaultProperties;

  /**
   * Flag deciding if preference change events should be broadcasted. Subclasses may set this value
   * to <code>false</code> to change the behavior when preference changes are broadcasted.
   */
  protected boolean isPreferenceChangeBroadcastEnabled = true;

  /** Creates a new preference store, containing no values. */
  public PreferenceStore() {
    defaultProperties = new Properties();
    properties = new Properties();
  }

  @Override
  public final void addPreferenceChangeListener(IPreferenceChangeListener listener) {
    changeListeners.addIfAbsent(listener);
  }

  @Override
  public final void removePreferenceChangeListener(IPreferenceChangeListener listener) {
    changeListeners.remove(listener);
  }

  @Override
  public final boolean getDefaultBoolean(final String name) {
    return convertValue(defaultProperties.get(name), boolean.class, true);
  }

  @Override
  public final int getDefaultInt(final String name) {
    return convertValue(defaultProperties.get(name), int.class, true);
  }

  @Override
  public final long getDefaultLong(final String name) {
    return convertValue(defaultProperties.get(name), long.class, true);
  }

  @Override
  public final String getDefaultString(final String name) {
    return convertValue(defaultProperties.get(name), String.class, true);
  }

  @Override
  public final void setDefault(final String name, final int value) {
    defaultProperties.put(name, Integer.valueOf(value));
  }

  @Override
  public final void setDefault(final String name, final long value) {
    defaultProperties.put(name, Long.valueOf(value));
  }

  @Override
  public final void setDefault(final String name, final String value) {
    defaultProperties.put(name, value == null ? DEFAULT_STRING : value);
  }

  @Override
  public final void setDefault(final String name, final boolean value) {
    defaultProperties.put(name, Boolean.valueOf(value));
  }

  @Override
  public final int getInt(final String name) {
    final Integer value = convertValue(getPreference(name), int.class, false);

    return value != null ? value : getDefaultInt(name);
  }

  @Override
  public final long getLong(final String name) {
    final Long value = convertValue(getPreference(name), long.class, false);

    return value != null ? value : getDefaultLong(name);
  }

  @Override
  public final boolean getBoolean(final String name) {
    final Boolean value = convertValue(getPreference(name), boolean.class, false);

    return value != null ? value : getDefaultBoolean(name);
  }

  @Override
  public final String getString(final String name) {
    final String value = convertValue(getPreference(name), String.class, false);

    return value != null ? value : getDefaultString(name);
  }

  @Override
  public final void setValue(final String name, final int value) {
    final int oldValue = convertValue(getPreference(name), int.class, true);

    setPreference(name, Integer.toString(value));
    checkAndTriggerPreferenceChange(name, oldValue, value);
  }

  @Override
  public final void setValue(final String name, final long value) {
    final long oldValue = convertValue(getPreference(name), long.class, true);

    setPreference(name, Long.toString(value));
    checkAndTriggerPreferenceChange(name, oldValue, value);
  }

  @Override
  public final void setValue(final String name, final boolean value) {
    final boolean oldValue = convertValue(getPreference(name), boolean.class, true);

    setPreference(name, Boolean.toString(value));
    checkAndTriggerPreferenceChange(name, oldValue, value);
  }

  @Override
  public final void setValue(final String name, final String value) {
    final String oldValue = convertValue(getPreference(name), String.class, true);

    setPreference(name, value);
    checkAndTriggerPreferenceChange(name, oldValue, value);
  }

  /**
   * Returns the preference for the given name (key). Clients may override this method to redirect
   * the storage.
   *
   * @param name the preference to look up
   * @return the value of the preference or <code>null</code>.
   */
  protected String getPreference(final String name) {
    return properties.getProperty(name);
  }

  /**
   * Sets the preference for the given name (key). Clients may override this method to redirect the
   * storage.
   *
   * @param name the preference to set
   * @param value the value of the preference or <code>null</code>
   */
  protected void setPreference(final String name, final String value) {
    properties.setProperty(name, value);
  }

  /**
   * Broadcasts the preference change to all registered listeners.
   *
   * @param event containing the details of the preference change
   */
  protected final void firePreferenceChange(final PreferenceChangeEvent event) {
    for (IPreferenceChangeListener listener : changeListeners) {
      try {
        listener.preferenceChange(event);
      } catch (RuntimeException e) {
        LOG.error(
            "invoking change listener " + listener + " with change event " + event + " failed", e);
      }
    }
  }

  private void checkAndTriggerPreferenceChange(
      final String name, final Object oldValue, final Object newValue) {

    if (!isPreferenceChangeBroadcastEnabled) return;

    if (oldValue == null && newValue == null) return;

    if (oldValue != null && oldValue.equals(newValue)) return;

    firePreferenceChange(new PreferenceChangeEvent(name, oldValue, newValue));
  }

  /**
   * Converts the given value to the given type. If useTypeDefault is false this method return null
   * on null values instead of default values !
   */
  @SuppressWarnings("unchecked")
  private static <T> T convertValue(
      final Object value, final Class<T> toType, boolean useTypeDefault) {

    Object defaultValue = null;

    if (toType == boolean.class) defaultValue = DEFAULT_BOOLEAN;
    else if (toType == int.class) defaultValue = DEFAULT_INT;
    else if (toType == long.class) defaultValue = DEFAULT_LONG;
    else if (toType == String.class) defaultValue = DEFAULT_STRING;
    else throw new IllegalArgumentException("unknown conversion type: " + toType.getName());

    if (value == null) return useTypeDefault ? (T) defaultValue : null;

    final String valueAsString = String.valueOf(value);

    Object result;

    try {
      if (toType == boolean.class) result = Boolean.valueOf(valueAsString);
      else if (toType == int.class) result = Integer.parseInt(valueAsString, 10);
      else if (toType == long.class) result = Long.parseLong(valueAsString, 10);
      else
        // string
        result = valueAsString;
    } catch (NumberFormatException e) {
      result = defaultValue;
    }

    assert result != null;

    return (T) result;
  }
}
