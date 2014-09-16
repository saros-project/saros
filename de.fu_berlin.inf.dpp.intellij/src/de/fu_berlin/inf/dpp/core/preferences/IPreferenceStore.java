package de.fu_berlin.inf.dpp.core.preferences;

public interface IPreferenceStore {

    /**
     * The default-default value for boolean preferences (<code>false</code>).
     */
    public static final boolean DEFAULT_BOOLEAN = false;

    /**
     * The default-default value for int preferences (<code>0</code>).
     */
    public static final int DEFAULT_INT = 0;

    /**
     * The default-default value for String preferences (<code>""</code>).
     */
    public static final String DEFAULT_STRING = "";

    int getInt(String value);

    boolean getBoolean(String key);

    String getString(String key);

    void setValue(String key, int value);

    void setValue(String key, boolean value);

    void setValue(String key, String value);

}
