package de.fu_berlin.inf.dpp.test.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

public class EclipseMemoryPreferenceStore implements IPreferenceStore {

  // TODO fire property changes

  private HashMap<String, Object> currentPreferences = new HashMap<String, Object>();
  private HashMap<String, Object> defaultPreferences = new HashMap<String, Object>();

  private List<IPropertyChangeListener> listeners = new ArrayList<IPropertyChangeListener>();

  @Override
  public void addPropertyChangeListener(IPropertyChangeListener listener) {
    listeners.add(listener);
  }

  @Override
  public void removePropertyChangeListener(IPropertyChangeListener listener) {
    listeners.remove(listener);
  }

  @Override
  public boolean contains(String name) {
    return currentPreferences.containsKey(name) || defaultPreferences.containsKey(name);
  }

  @Override
  public void firePropertyChangeEvent(String name, Object oldValue, Object newValue) {
    for (IPropertyChangeListener listener : listeners)
      listener.propertyChange(new PropertyChangeEvent(this, name, oldValue, newValue));
  }

  @Override
  public boolean getBoolean(String name) {
    Object object = currentPreferences.get(name);

    if (object == null) return getDefaultBoolean(name);

    try {
      return (Boolean) object;
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public double getDouble(String name) {
    Object object = currentPreferences.get(name);

    if (object == null) return getDefaultDouble(name);

    try {
      return (Double) object;
    } catch (Exception e) {
      return 0D;
    }
  }

  @Override
  public float getFloat(String name) {
    Object object = currentPreferences.get(name);

    if (object == null) return getDefaultFloat(name);

    try {
      return (Float) object;
    } catch (Exception e) {
      return 0F;
    }
  }

  @Override
  public int getInt(String name) {
    Object object = currentPreferences.get(name);

    if (object == null) return getDefaultInt(name);

    try {
      return (Integer) object;
    } catch (Exception e) {
      return 0;
    }
  }

  @Override
  public long getLong(String name) {
    Object object = currentPreferences.get(name);

    if (object == null) return getDefaultLong(name);

    try {
      return (Long) object;
    } catch (Exception e) {
      return 0L;
    }
  }

  @Override
  public String getString(String name) {
    Object object = currentPreferences.get(name);

    if (object == null) return getDefaultString(name);

    try {
      return (String) object;
    } catch (Exception e) {
      return "";
    }
  }

  @Override
  public boolean getDefaultBoolean(String name) {
    Object object = defaultPreferences.get(name);

    if (object == null) return false;

    try {
      return (Boolean) object;
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public double getDefaultDouble(String name) {
    Object object = defaultPreferences.get(name);

    if (object == null) return 0D;

    try {
      return (Double) object;
    } catch (Exception e) {
      return 0D;
    }
  }

  @Override
  public float getDefaultFloat(String name) {
    Object object = defaultPreferences.get(name);

    if (object == null) return 0F;

    try {
      return (Float) object;
    } catch (Exception e) {
      return 0F;
    }
  }

  @Override
  public int getDefaultInt(String name) {
    Object object = defaultPreferences.get(name);

    if (object == null) return 0;

    try {
      return (Integer) object;
    } catch (Exception e) {
      return 0;
    }
  }

  @Override
  public long getDefaultLong(String name) {
    Object object = defaultPreferences.get(name);

    if (object == null) return 0L;

    try {
      return (Long) object;
    } catch (Exception e) {
      return 0L;
    }
  }

  @Override
  public String getDefaultString(String name) {
    Object object = defaultPreferences.get(name);

    if (object == null) return "";

    try {
      return (String) object;
    } catch (Exception e) {
      return "";
    }
  }

  @Override
  public boolean isDefault(String name) {
    Object object = currentPreferences.get(name);
    return object != null && object.equals(defaultPreferences.get(name));
  }

  @Override
  public boolean needsSaving() {
    return false;
  }

  @Override
  public void putValue(String name, String value) {
    currentPreferences.put(name, value);
  }

  @Override
  public void setDefault(String name, double value) {
    setDefault(name, (Object) value);
  }

  @Override
  public void setDefault(String name, float value) {
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

  @Override
  public void setDefault(String name, boolean value) {
    setDefault(name, (Object) value);
  }

  private void setDefault(String name, Object value) {
    Object old = defaultPreferences.put(name, value);
    if (old != null && old.equals(currentPreferences.get(name)))
      currentPreferences.put(name, value);
  }

  @Override
  public void setToDefault(String name) {
    currentPreferences.put(name, defaultPreferences.get(name));
  }

  @Override
  public void setValue(String name, double value) {
    currentPreferences.put(name, value);
  }

  @Override
  public void setValue(String name, float value) {
    currentPreferences.put(name, value);
  }

  @Override
  public void setValue(String name, int value) {
    currentPreferences.put(name, value);
  }

  @Override
  public void setValue(String name, long value) {
    currentPreferences.put(name, value);
  }

  @Override
  public void setValue(String name, String value) {
    currentPreferences.put(name, value);
  }

  @Override
  public void setValue(String name, boolean value) {
    currentPreferences.put(name, value);
  }
}
