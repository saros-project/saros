package de.fu_berlin.inf.dpp.preferences;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * Adapts Eclipse's preference storing and change notification mechanism to Saros's IPreferences
 * interface.
 */
public class EclipsePreferenceStoreAdapter
    implements de.fu_berlin.inf.dpp.preferences.IPreferenceStore {

  private final IPreferenceStore delegate;
  private final List<IPreferenceChangeListener> listeners =
      new CopyOnWriteArrayList<IPreferenceChangeListener>();

  private final IPropertyChangeListener propertyChangeListener =
      new IPropertyChangeListener() {

        @Override
        public void propertyChange(final PropertyChangeEvent event) {
          final PreferenceChangeEvent eventToFire =
              new PreferenceChangeEvent(
                  event.getProperty(), event.getOldValue(), event.getNewValue());

          for (final IPreferenceChangeListener listener : listeners)
            listener.preferenceChange(eventToFire);
        }
      };

  /**
   * Constructs an EclipsePreferenceStoreAdapter with an {@link IPreferenceStore}
   *
   * @param delegate
   */
  public EclipsePreferenceStoreAdapter(final IPreferenceStore delegate) {
    this.delegate = delegate;
    this.delegate.addPropertyChangeListener(propertyChangeListener);
  }

  @Override
  public void addPreferenceChangeListener(final IPreferenceChangeListener listener) {
    listeners.add(listener);
  }

  @Override
  public void removePreferenceChangeListener(final IPreferenceChangeListener listener) {
    listeners.remove(listener);
  }

  @Override
  public boolean getBoolean(String name) {
    return delegate.getBoolean(name);
  }

  @Override
  public boolean getDefaultBoolean(String name) {
    return delegate.getDefaultBoolean(name);
  }

  @Override
  public int getInt(String name) {
    return delegate.getInt(name);
  }

  @Override
  public int getDefaultInt(String name) {
    return delegate.getDefaultInt(name);
  }

  @Override
  public long getLong(String name) {
    return delegate.getLong(name);
  }

  @Override
  public long getDefaultLong(String name) {
    return delegate.getDefaultLong(name);
  }

  @Override
  public String getString(String name) {
    return delegate.getString(name);
  }

  @Override
  public String getDefaultString(String name) {
    return delegate.getDefaultString(name);
  }

  @Override
  public void setValue(String name, int value) {
    delegate.setValue(name, value);
  }

  @Override
  public void setValue(String name, long value) {
    delegate.setValue(name, value);
  }

  @Override
  public void setValue(String name, String value) {
    delegate.setValue(name, value);
  }

  @Override
  public void setValue(String name, boolean value) {
    delegate.setValue(name, value);
  }

  @Override
  public void setDefault(String name, int value) {
    delegate.setDefault(name, value);
  }

  @Override
  public void setDefault(String name, long value) {
    delegate.setDefault(name, value);
  }

  @Override
  public void setDefault(String name, String value) {
    delegate.setDefault(name, value);
  }

  @Override
  public void setDefault(String name, boolean value) {
    delegate.setDefault(name, value);
  }

  /**
   * Returns the {@linkplain IPreferenceStore preference store} for this adapter.
   *
   * @return the preference store of this adapter
   */
  public IPreferenceStore getPreferenceStore() {
    return delegate;
  }
}
