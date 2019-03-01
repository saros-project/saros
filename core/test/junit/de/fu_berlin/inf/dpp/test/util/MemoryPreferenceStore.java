package de.fu_berlin.inf.dpp.test.util;

import de.fu_berlin.inf.dpp.preferences.IPreferenceChangeListener;
import de.fu_berlin.inf.dpp.preferences.IPreferenceStore;
import java.util.HashMap;

/**
 * A simple in-memory implementation of the {@link IPreferenceStore} interface. It currently doesn't
 * support preference change listeners.
 */
public class MemoryPreferenceStore implements IPreferenceStore {

  private HashMap<String, Object> currentValues = new HashMap<String, Object>();
  private HashMap<String, Object> defaultValues = new HashMap<String, Object>();
  private HashMap<Class<?>, Object> defaultDefaultValues = new HashMap<Class<?>, Object>();

  public MemoryPreferenceStore() {
    defaultDefaultValues.put(Boolean.class, false);
    defaultDefaultValues.put(Integer.class, 0);
    defaultDefaultValues.put(Long.class, 0L);
    defaultDefaultValues.put(String.class, "");
  }

  @Override
  public boolean getBoolean(String name) {
    return getValue(name, Boolean.class);
  }

  @Override
  public int getInt(String name) {
    return getValue(name, Integer.class);
  }

  @Override
  public long getLong(String name) {
    return getValue(name, Long.class);
  }

  @Override
  public String getString(String name) {
    return getValue(name, String.class);
  }

  private <T> T getValue(String name, Class<T> type) {
    Object value = currentValues.get(name);
    if (value == null) {
      return getDefaultValue(name, type);
    } else if (type.isInstance(value)) {
      return type.cast(value);
    } else {
      return getDefaultDefaultValue(type);
    }
  }

  @Override
  public boolean getDefaultBoolean(String name) {
    return getDefaultValue(name, Boolean.class);
  }

  @Override
  public int getDefaultInt(String name) {
    return getDefaultValue(name, Integer.class);
  }

  @Override
  public long getDefaultLong(String name) {
    return getDefaultValue(name, Long.class);
  }

  @Override
  public String getDefaultString(String name) {
    return getDefaultValue(name, String.class);
  }

  private <T> T getDefaultValue(String name, Class<T> type) {
    Object value = currentValues.get(name);
    if (type.isInstance(value)) {
      return type.cast(value);
    } else {
      return getDefaultDefaultValue(type);
    }
  }

  private <T> T getDefaultDefaultValue(Class<T> type) {
    return type.cast(defaultDefaultValues.get(type));
  }

  @Override
  public void setValue(String name, boolean value) {
    currentValues.put(name, value);
  }

  @Override
  public void setValue(String name, int value) {
    currentValues.put(name, value);
  }

  @Override
  public void setValue(String name, long value) {
    currentValues.put(name, value);
  }

  @Override
  public void setValue(String name, String value) {
    currentValues.put(name, value);
  }

  @Override
  public void setDefault(String name, boolean value) {
    setDefault(name, (Object) value);
  }

  @Override
  public void setDefault(String name, int value) {
    setDefault(name, (Object) value);
  }

  @Override
  public void setDefault(String name, long value) {
    setDefault(name, (Object) value);
  }

  @Override
  public void setDefault(String name, String value) {
    setDefault(name, (Object) value);
  }

  private void setDefault(String name, Object value) {
    Object old = defaultValues.put(name, value);
    if (old != null && old.equals(currentValues.get(name))) {
      currentValues.put(name, value);
    }
  }

  @Override
  public void addPreferenceChangeListener(IPreferenceChangeListener listener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removePreferenceChangeListener(IPreferenceChangeListener listener) {
    throw new UnsupportedOperationException();
  }
}
