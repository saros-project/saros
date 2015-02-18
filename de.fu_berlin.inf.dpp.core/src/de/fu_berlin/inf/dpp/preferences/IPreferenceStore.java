package de.fu_berlin.inf.dpp.preferences;

/**
 * Implementations of IPreferences store key-value pairs and notify listeners
 * upon value changes.
 * 
 * There is a default value and a current value for each preference. If there is
 * no current value, the default value will be returned. If there is no default
 * value for the type indicated by the method's name will be returned.
 * <ul>
 * <li><code>boolean</code> = <code>false</code></li>
 * <li><code>double</code> = <code>0.0</code></li>
 * <li><code>float</code> = <code>0.0f</code></li>
 * <li><code>int</code> = <code>0</code></li>
 * <li><code>long</code> = <code>0</code></li>
 * <li><code>String</code> = <code>""</code> (the empty string)</li>
 * </ul>
 * This default-default values will also be returned when the given preference
 * cannot be converted into this required type.
 * 
 * The typical life-cycle starts with setting the default values and then goes
 * on with setting the preferences that differ from the defaults.
 * 
 * After the current value was changed through <code>setValue()</code>, all
 * registered {@linkplain IPreferenceChangeListener listeners} will be notified.
 */
public interface IPreferenceStore {

    /**
     * Registers a preference change listener that will be notified after each
     * setValue() call.
     * 
     * @param listener
     *            a preference change listener
     */
    public void addPreferenceChangeListener(IPreferenceChangeListener listener);

    /**
     * removes a preference change listener. It has no effect, if the listener
     * is not registered.
     * 
     * @param listener
     *            the listener must not be <code>null</code>
     */
    public void removePreferenceChangeListener(
        IPreferenceChangeListener listener);

    /**
     * Returns the current value of the named preference as a Boolean. If there
     * is no preference or the value can not be converted to a boolean, it
     * returns <code>false</code>.
     * 
     * @param name
     *            of the named preference
     * @return the current boolean value
     */
    public boolean getBoolean(String name);

    /**
     * Returns the default value of the named preference as a Boolean. If there
     * is no preference or the value can not be converted to a boolean, it
     * returns <code>false</code>.
     * 
     * @param name
     *            of the named preference
     * @return the default boolean value
     */
    public boolean getDefaultBoolean(String name);

    /**
     * Returns the current value of the named preference as a Integer. If there
     * is no preference or the value can not be converted to a integer, it
     * returns <code>0</code>.
     * 
     * @param name
     *            of the named preference
     * @return the current integer value
     */
    public int getInt(String name);

    /**
     * Returns the default value of the named preference as a Integer. If there
     * is no preference or the value can not be converted to a integer, it
     * returns <code>0</code>.
     * 
     * @param name
     *            of the named preference
     * @return the default integer value
     */
    public int getDefaultInt(String name);

    /**
     * Returns the current value of the named preference as a Long. If there is
     * no preference or the value can not be converted to a long, it returns
     * <code>0</code>.
     * 
     * @param name
     *            of the named preference
     * @return the current long value
     */
    public long getLong(String name);

    /**
     * Returns the default value of the named preference as a Long. If there is
     * no preference or the value can not be converted to a long, it returns
     * <code>0</code>.
     * 
     * @param name
     *            of the named preference
     * @return the default long value
     */
    public long getDefaultLong(String name);

    /**
     * Returns the current value of the named preference as a String. If there
     * is no preference or the value can not be converted to a String, it
     * returns <code>""(empty String)</code>.
     * 
     * @param name
     *            of the named preference
     * @return the default String value
     */
    public String getString(String name);

    /**
     * Returns the default value of the named preference as a String. If there
     * is no preference or the value can not be converted to a String, it
     * returns <code>""(empty String)</code>.
     * 
     * @param name
     *            of the named preference
     * @return the default String value
     */
    public String getDefaultString(String name);

    /**
     * Sets the current value to an Integer valued preference for the given
     * name. If the new value differ from the old value, a preferenceChangeEvent
     * will be reported.
     * 
     * @param name
     *            of the named preference
     * @param value
     *            new current value
     */
    public void setValue(String name, int value);

    /**
     * Sets the current value to an Long valued preference for the given name.
     * If the new value differ from the old value, a preferenceChangeEvent will
     * be reported.
     * 
     * @param name
     *            of the named preference
     * @param value
     *            new current value
     */
    public void setValue(String name, long value);

    /**
     * Sets the current value to an String valued preference for the given name.
     * If the new value differ from the old value, a preferenceChangeEvent will
     * be reported.
     * 
     * @param name
     *            of the named preference
     * @param value
     *            new current value, can not be <code>null</code>
     */
    public void setValue(String name, String value);

    /**
     * Sets the current value to an Boolean valued preference for the given
     * name. If the new value differ from the old value, a preferenceChangeEvent
     * will be reported.
     * 
     * @param name
     *            of the named preference
     * @param value
     *            new current value
     */
    void setValue(String name, boolean value);
}
