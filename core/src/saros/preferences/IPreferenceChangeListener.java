package saros.preferences;

/**
 * Preference changes will be handled by implementations of this listener.
 *
 * <p>The normal usage is an anonymous implementation of this listener
 *
 * <pre>
 * IPreferenceChangeListener listener = new IPreferenceChangeListener() {
 *     public void preferenceChange(PreferenceChangeEvent event) {
 *         // handle preference change here
 *     }
 * };
 * </pre>
 */
public interface IPreferenceChangeListener {

  /**
   * This method is called, when a preference is added or the value has been changed.
   *
   * @param event is describing the modified preference with there values
   */
  public void preferenceChange(PreferenceChangeEvent event);
}
