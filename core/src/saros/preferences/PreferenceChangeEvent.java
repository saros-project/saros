package saros.preferences;

/**
 * This {@link PreferenceChangeEvent} is describing a changed preference.
 *
 * <p>This event is given to a listener and stores the old value, the new value and the name of the
 * changed preference.
 *
 * @see IPreferenceChangeListener
 * @see Preferences
 */
public class PreferenceChangeEvent {

  private final String preferenceName;
  private final Object oldValue;
  private final Object newValue;

  /**
   * Constructor for a PreferenceChangeEvent
   *
   * @param preferenceName name of the preference
   * @param oldValue old value of the preference
   * @param newValue new value of the preference
   */
  public PreferenceChangeEvent(
      final String preferenceName, final Object oldValue, final Object newValue) {
    this.preferenceName = preferenceName;
    this.oldValue = oldValue;
    this.newValue = newValue;
  }

  /** @return name of the modified preference */
  public String getPreferenceName() {
    return preferenceName;
  }

  /** @return old value of the modified preference, <code>null</code> if there was no old value */
  public Object getOldValue() {
    return oldValue;
  }

  /** @return new value of the modified preference, can be <code>null</code> */
  public Object getNewValue() {
    return newValue;
  }

  @Override
  public String toString() {
    return "PreferenceChangeEvent [preferenceName="
        + preferenceName
        + ", oldValue="
        + oldValue
        + ", newValue="
        + newValue
        + "]";
  }
}
