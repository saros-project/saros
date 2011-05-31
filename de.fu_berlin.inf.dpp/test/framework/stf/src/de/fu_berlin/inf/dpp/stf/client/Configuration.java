package de.fu_berlin.inf.dpp.stf.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.log4j.Logger;

public final class Configuration {

    private final static String DEFAULT_CONFIGURATION_FILE = "de/fu_berlin/inf/dpp/stf/client/configuration.properties";
    private final static String OPTIONAL_CONFIGURATION_FILES_PROPERTY = "de.fu_berlin.inf.dpp.stf.client.configuration.files";

    public final static boolean DEVELOPMODE = true;

    private final static Configuration INSTANCE = new Configuration();

    private Logger log = Logger.getLogger(Configuration.class);

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
        InputStream in = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream(DEFAULT_CONFIGURATION_FILE);

        log.debug("loading internal property file");
        if (in != null) {
            try {
                loadProperties(in);
            } catch (IOException e) {
                log.debug("error while reading internal property file", e);
            } finally {
                try {
                    in.close();
                } catch (IOException ignore) {
                    //
                }
            }
        } else
            log.debug("could not find internal property file "
                + DEFAULT_CONFIGURATION_FILE);

        String configurationFiles = System.getProperty(
            OPTIONAL_CONFIGURATION_FILES_PROPERTY, "");

        for (String configurationFile : configurationFiles
            .split(File.pathSeparator)) {

            configurationFile = configurationFile.trim();

            if (configurationFile.length() == 0)
                continue;

            in = null;
            log.debug("loading external property file " + configurationFile);
            try {
                in = new FileInputStream(new File(configurationFile));
                loadProperties(in);
            } catch (FileNotFoundException e) {
                log.debug("could not find external property file "
                    + configurationFile, e);
            } catch (IOException e) {
                log.debug("error while reading property file", e);
            } finally {
                try {
                    if (in != null)
                        in.close();
                } catch (IOException ignore) {
                    //
                }
            }
        }
    }

    private void loadProperties(InputStream in) throws IOException {

        Properties temp = new Properties();

        temp.load(in);

        for (Entry<Object, Object> entry : temp.entrySet())
            properties.put(entry.getKey().toString().toUpperCase(), entry
                .getValue().toString());
    }
}
