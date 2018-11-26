package de.fu_berlin.inf.dpp.stf.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Configuration {

  private static final Logger LOGGER = Logger.getLogger(Configuration.class.getName());

  private static final String DEFAULT_CONFIGURATION_FILE =
      "de/fu_berlin/inf/dpp/stf/client/configuration.properties";
  private static final String OPTIONAL_CONFIGURATION_FILES_PROPERTY =
      "de.fu_berlin.inf.dpp.stf.client.configuration.files";

  private static final Configuration INSTANCE = new Configuration();

  private Properties properties;

  private Configuration() {
    properties = new Properties();
    loadProperties();
  }

  public static Configuration getInstance() {
    return INSTANCE;
  }

  public Object get(Object key) {
    return properties.get(key);
  }

  private void loadProperties() {
    InputStream in =
        Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream(DEFAULT_CONFIGURATION_FILE);

    LOGGER.info("loading internal property file");
    if (in != null) {
      try {
        loadProperties(in);
      } catch (IOException e) {
        LOGGER.log(Level.SEVERE, "error while reading internal property file", e);
      } finally {
        try {
          in.close();
        } catch (IOException ignore) {
          LOGGER.log(Level.WARNING, ignore.getMessage(), ignore);
        }
      }
    } else
      LOGGER.log(
          Level.WARNING, "could not find internal property file: " + DEFAULT_CONFIGURATION_FILE);

    String configurationFiles = System.getProperty(OPTIONAL_CONFIGURATION_FILES_PROPERTY, "");

    for (String configurationFile : configurationFiles.split(File.pathSeparator)) {

      configurationFile = configurationFile.trim();

      if (configurationFile.length() == 0) continue;

      in = null;
      LOGGER.info("loading external property file: " + configurationFile);
      try {
        in = new FileInputStream(new File(configurationFile));
        loadProperties(in);
      } catch (FileNotFoundException e) {
        LOGGER.log(Level.WARNING, "could not find external property file " + configurationFile, e);
      } catch (IOException e) {
        LOGGER.log(Level.SEVERE, "error while reading property file", e);
      } finally {
        try {
          if (in != null) in.close();
        } catch (IOException ignore) {
          LOGGER.log(Level.WARNING, ignore.getMessage(), ignore);
        }
      }
    }
  }

  private void loadProperties(InputStream in) throws IOException {

    Properties temp = new Properties();

    temp.load(in);

    for (Entry<Object, Object> entry : temp.entrySet())
      properties.put(entry.getKey().toString().toUpperCase(), entry.getValue().toString());
  }
}
