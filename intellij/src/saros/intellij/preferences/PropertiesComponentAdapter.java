package saros.intellij.preferences;

import com.intellij.ide.util.PropertiesComponent;
import java.nio.charset.Charset;
import org.apache.commons.codec.binary.Base64;
import saros.preferences.PreferenceStore;

/**
 * This class adapts the {@link PropertiesComponent} to the IPreferenceStore interface.
 *
 * <p>The actual values can be found in $IDEA_HOME/config/options/options.xml and are prefixed with
 * {@link #PROPERTY_PREFIX}.
 */
public class PropertiesComponentAdapter extends PreferenceStore {

  private static final String PROPERTY_PREFIX = "saros.config.";

  private static final Charset PROPERTY_CHARSET = Charset.forName("UTF-8");
  private static final Charset BASE64_CHARSET = Charset.forName("ISO-8859-1");

  private final PropertiesComponent delegate;

  /** Creates a new PreferenceStore and initializes the PropertiesComponent. */
  public PropertiesComponentAdapter() {
    delegate = getPropertiesComponent();
  }

  /** @return a PropertiesComponent that stores keys for the whole application. */
  static PropertiesComponent getPropertiesComponent() {
    return PropertiesComponent.getInstance();
  }

  @Override
  protected final void setPreference(final String name, final String value) {
    final String encodedValue =
        new String(Base64.encodeBase64(value.getBytes(PROPERTY_CHARSET)), BASE64_CHARSET);

    delegate.setValue(PROPERTY_PREFIX + name, encodedValue);
  }

  @Override
  protected String getPreference(final String name) {
    final String value = delegate.getValue(PROPERTY_PREFIX + name);

    if (value == null) return null;

    return new String(Base64.decodeBase64(value.getBytes(BASE64_CHARSET)), PROPERTY_CHARSET);
  }
}
