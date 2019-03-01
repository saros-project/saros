package saros.server.preferences;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.InvalidPropertiesFormatException;
import org.apache.log4j.Logger;
import saros.preferences.PreferenceStore;

/** Preference store allowing the storing and retrieving of the preferences to and from a file. */
public final class PersistencePreferenceStore extends PreferenceStore {

  private static final Logger LOG = Logger.getLogger(PersistencePreferenceStore.class);

  private File preferenceFile;

  /**
   * Sets the underlying storage file for the store to save and load data.
   *
   * <p>If a reload of the data is desired the file have to exists, otherwise it will be created
   * when the current properties are saved.
   *
   * <p><b>Note:</b> Setting the file to <code>null</code> will <b>NOT</b> discard any existing
   * properties.
   *
   * @param file the file to load and store data or <code>null</code>
   * @param loadProperties if <code>true</code> the current properties will be overwritten with the
   *     properties provided by the given file
   */
  public synchronized void setStorageFile(final File file, final boolean loadProperties) {
    preferenceFile = file;

    if (loadProperties) load();
  }

  /**
   * Loads the property data form the current storage file. Existing properties will be discarded.
   *
   * @see #setStorageFile
   */
  public synchronized void load() {

    if (preferenceFile == null) return;

    try (InputStream in = new BufferedInputStream(new FileInputStream(preferenceFile))) {

      properties.clear();
      properties.loadFromXML(in);
    } catch (InvalidPropertiesFormatException e) {
      LOG.error(
          "failed to load properties, file " + preferenceFile + " contains malformed data", e);
    } catch (FileNotFoundException e) {
      LOG.error("failed to load properties, file " + preferenceFile + " does not exists", e);
    } catch (IOException e) {
      LOG.error("failed to load properties from file: " + preferenceFile, e);
    }
  }

  /**
   * Saves the property data to the current storage file.
   *
   * @see #setStorageFile
   */
  public synchronized void save() {

    if (preferenceFile == null) return;

    try (OutputStream out = new BufferedOutputStream(new FileOutputStream(preferenceFile))) {
      properties.storeToXML(out, null);

    } catch (FileNotFoundException e) {
      LOG.error(
          "failed to save properties, file "
              + preferenceFile
              + " is either a directory or could not be created",
          e);
    } catch (IOException e) {
      LOG.error("failed to save properties to file: " + preferenceFile, e);
    }
  }
}
