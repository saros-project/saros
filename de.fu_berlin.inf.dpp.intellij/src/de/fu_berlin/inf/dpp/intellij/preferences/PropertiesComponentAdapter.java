package de.fu_berlin.inf.dpp.intellij.preferences;

import com.intellij.ide.util.PropertiesComponent;
import de.fu_berlin.inf.dpp.preferences.IPreferenceChangeListener;
import de.fu_berlin.inf.dpp.preferences.IPreferenceStore;
import org.apache.commons.codec.binary.Base64;

import java.nio.charset.Charset;
import java.util.Properties;

/**
 * This class adapts the {@link PropertiesComponent} to the IPreferenceStore
 * interface.
 * <p/>
 * The actual values can be found in $IDEA_HOME/config/options/options.xml and
 * are prefixed with {@link #PROPERTY_PREFIX}.
 */
public class PropertiesComponentAdapter implements IPreferenceStore {

    private static final String PROPERTY_PREFIX = "de.fu_berlin.inf.dpp.config.";

    private static final Charset PROPERTY_CHARSET = Charset.forName("UTF-8");
    private static final Charset BASE64_CHARSET = Charset.forName("ISO-8859-1");

    private PropertiesComponent properties;

    private Properties defaultProperties;

    /**
     * The default-default value for boolean preferences (<code>false</code>).
     */
    public static final boolean DEFAULT_BOOLEAN = false;

    /**
     * The default-default value for int preferences (<code>0</code>).
     */
    public static final int DEFAULT_INT = 0;

    /**
     * The default-default value for long preferences (<code>0</code>).
     */
    public static final long DEFAULT_LONG = 0L;

    /**
     * The default-default value for String preferences (<code>""</code>).
     */
    public static final String DEFAULT_STRING = "";

    /**
     * Creates a new PreferenceStore and initializes the PropertiesComponent.
     */
    public PropertiesComponentAdapter() {
        defaultProperties = new Properties();
        properties = getPropertiesComponent();
    }

    /**
     * @return a PropertiesComponent that stores keys for the whole application.
     */
    static PropertiesComponent getPropertiesComponent() {
        return PropertiesComponent.getInstance();
    }

    @Override
    public void addPreferenceChangeListener(
        IPreferenceChangeListener listener) {
        throw new UnsupportedOperationException(
            "IntelliJ PreferenceStore does not support listening for preference changes.");
    }

    @Override
    public void removePreferenceChangeListener(
        IPreferenceChangeListener listener) {
        throw new UnsupportedOperationException(
            "IntelliJ PreferenceStore does not support listening for preference changes.");
    }

    @Override
    public boolean getDefaultBoolean(String name) {
        return convertValue(defaultProperties.get(name), boolean.class, true);
    }

    @Override
    public int getDefaultInt(String name) {
        return convertValue(defaultProperties.get(name), int.class, true);
    }

    @Override
    public long getDefaultLong(String name) {
        return convertValue(defaultProperties.get(name), long.class, true);
    }

    @Override
    public String getDefaultString(String name) {
        return convertValue(defaultProperties.get(name), String.class, true);
    }

    @Override
    public void setDefault(String name, int value) {
        defaultProperties.put(name, Integer.valueOf(value));
    }

    @Override
    public void setDefault(String name, long value) {
        defaultProperties.put(name, Long.valueOf(value));
    }

    @Override
    public void setDefault(String name, String value) {
        defaultProperties.put(name, value == null ? DEFAULT_STRING : value);
    }

    @Override
    public void setDefault(String name, boolean value) {
        defaultProperties.put(name, Boolean.valueOf(value));
    }

    @Override
    public int getInt(String name) {
        final Integer value = convertValue(getPropertyValue(name), int.class,
            false);

        return value != null ? value : getDefaultInt(name);
    }

    @Override
    public long getLong(String name) {
        final Long value = convertValue(getPropertyValue(name), long.class,
            false);

        return value != null ? value : getDefaultLong(name);
    }

    @Override
    public boolean getBoolean(String name) {
        final Boolean value = convertValue(getPropertyValue(name),
            boolean.class, false);

        return value != null ? value : getDefaultBoolean(name);
    }

    @Override
    public String getString(String name) {
        final String value = convertValue(getPropertyValue(name), String.class,
            false);

        return value != null ? value : getDefaultString(name);
    }

    @Override
    public void setValue(String name, int value) {
        setValue(name, Integer.toString(value));
    }

    @Override
    public void setValue(String name, long value) {
        setValue(name, Long.toString(value));
    }

    @Override
    public void setValue(String name, boolean value) {
        setValue(name, Boolean.toString(value));
    }

    @Override
    public void setValue(String name, String value) {
        setPropertyValue(name, value);
    }

    private void setPropertyValue(final String name, final String value) {
        final String encodedValue = new String(
            Base64.encodeBase64(value.getBytes(PROPERTY_CHARSET)),
            BASE64_CHARSET);

        properties.setValue(PROPERTY_PREFIX + name, encodedValue);
    }

    private String getPropertyValue(final String name) {
        final String value = properties.getValue(PROPERTY_PREFIX + name);

        if (value == null)
            return null;

        return new String(Base64.decodeBase64(value.getBytes(BASE64_CHARSET)),
            PROPERTY_CHARSET);
    }

    /**
     * Converts the given value to the given type. If useTypeDefault is false
     * this method return null on null values instead of default values !
     */
    @SuppressWarnings("unchecked")
    private static <T> T convertValue(final Object value, final Class<T> toType,
        boolean useTypeDefault) {

        Object defaultValue = null;

        if (toType == boolean.class)
            defaultValue = DEFAULT_BOOLEAN;
        else if (toType == int.class)
            defaultValue = DEFAULT_INT;
        else if (toType == long.class)
            defaultValue = DEFAULT_LONG;
        else if (toType == String.class)
            defaultValue = DEFAULT_STRING;
        else
            throw new IllegalArgumentException(
                "unknown conversion type: " + toType.getName());

        if (value == null && useTypeDefault)
            return (T) defaultValue;
        else if (value == null && !useTypeDefault)
            return null;

        final String valueAsString = String.valueOf(value);

        Object result;

        try {
            if (toType == boolean.class)
                result = Boolean.valueOf(valueAsString);
            else if (toType == int.class)
                result = Integer.parseInt(valueAsString, 10);
            else if (toType == long.class)
                result = Long.parseLong(valueAsString, 10);
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
