package de.fu_berlin.inf.dpp.test.util;

import java.util.HashMap;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/** @author Stefan Rossbach */
public class MemoryPreferences implements Preferences {

  private HashMap<String, Object> preferences = new HashMap<String, Object>();
  private boolean allowPut = true;
  private boolean allowGet = true;

  public void allowPutOperation(boolean allow) {
    this.allowPut = allow;
  }

  public void allowGetOperation(boolean allow) {
    this.allowGet = allow;
  }

  public void checkGetOperation() {
    if (!this.allowGet) throw new IllegalStateException("get disabled");
  }

  public void checkPutOperation() {
    if (!this.allowPut) throw new IllegalStateException("put disabled");
  }

  @Override
  public String absolutePath() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String[] childrenNames() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    preferences.clear();
  }

  @Override
  public String get(String key, String def) {
    checkGetOperation();

    Object object = preferences.get(key);

    if (object == null) return def;

    return (String) object;
  }

  @Override
  public boolean getBoolean(String key, boolean def) {
    checkGetOperation();

    Object object = preferences.get(key);

    if (object == null) return def;

    return (Boolean) object;
  }

  @Override
  public byte[] getByteArray(String key, byte[] def) {
    checkGetOperation();

    Object object = preferences.get(key);

    if (object == null) return def;

    return (byte[]) object;
  }

  @Override
  public double getDouble(String key, double def) {
    checkGetOperation();

    Object object = preferences.get(key);

    if (object == null) return def;

    return (Double) object;
  }

  @Override
  public float getFloat(String key, float def) {
    checkGetOperation();

    Object object = preferences.get(key);

    if (object == null) return def;

    return (Float) object;
  }

  @Override
  public int getInt(String key, int def) {
    checkGetOperation();

    Object object = preferences.get(key);

    if (object == null) return def;

    return (Integer) object;
  }

  @Override
  public long getLong(String key, long def) {
    checkGetOperation();

    Object object = preferences.get(key);

    if (object == null) return def;

    return (Long) object;
  }

  @Override
  public String[] keys() {
    return preferences.keySet().toArray(new String[0]);
  }

  @Override
  public String name() {
    return this.getClass().getName();
  }

  @Override
  public Preferences node(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean nodeExists(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Preferences parent() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void put(String key, String value) {
    checkPutOperation();
    preferences.put(key, value);
  }

  @Override
  public void putBoolean(String key, boolean value) {
    checkPutOperation();
    preferences.put(key, value);
  }

  @Override
  public void putByteArray(String key, byte[] value) {
    checkPutOperation();
    preferences.put(key, value);
  }

  @Override
  public void putDouble(String key, double value) {
    checkPutOperation();
    preferences.put(key, value);
  }

  @Override
  public void putFloat(String key, float value) {
    checkPutOperation();
    preferences.put(key, value);
  }

  @Override
  public void putInt(String key, int value) {
    checkPutOperation();
    preferences.put(key, value);
  }

  @Override
  public void putLong(String key, long value) {
    checkPutOperation();
    preferences.put(key, value);
  }

  @Override
  public void remove(String key) {
    preferences.remove(key);
  }

  @Override
  public void removeNode() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void flush() throws BackingStoreException {
    // do nothing
  }

  @Override
  public void sync() throws BackingStoreException {
    // do nothing
  }
}
