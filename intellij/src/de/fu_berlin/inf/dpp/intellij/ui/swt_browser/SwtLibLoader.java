package de.fu_berlin.inf.dpp.intellij.ui.swt_browser;

import com.intellij.ide.plugins.cl.PluginClassLoader;
import com.intellij.openapi.application.PathManager;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.log4j.Logger;

/**
 * This class loads the appropriate swt library from the swt plugin depending on the operating
 * system and the processor architecture.
 *
 * <p>This class is not intended to be subclassed.
 */
public class SwtLibLoader {

  private static final Logger LOG = Logger.getLogger(SwtLibLoader.class);

  private static final String JAR_BASE_FILENAME = "swt-4.4-";

  /**
   * This methods adds the SWT library to the plugin class loader responsible for the Saros IntelliJ
   * plug-in.
   *
   * <p>For this to work the SWT libary plugin has to be installed in IntelliJ. Currently Windows,
   * Mac OS, and Linux is supported, both 32 and 64 bit.
   *
   * <p>This method should only be called once.
   *
   * @throws IllegalStateException if the actual OS arch combination is not supported or the SWT
   *     libary plug-in is not installed correctly.
   */
  public static void loadSwtLib() {
    PluginClassLoader pluginClassLoader = obtainPluginClassLoader();
    URL swtFileUrl = getSwtLibUrl();

    // TODO maybe check if URL has already been added or make this call threadsafe
    pluginClassLoader.addURL(swtFileUrl);
    LOG.info("Added " + swtFileUrl.getPath() + " to the classpath");
  }

  private static PluginClassLoader obtainPluginClassLoader() {
    ClassLoader classLoader = SwtLibLoader.class.getClassLoader();
    if (!(classLoader instanceof PluginClassLoader)) {
      throw new RuntimeException("unable to get hold of the plugin classloader");
    }
    return (PluginClassLoader) classLoader;
  }

  private static URL getSwtLibUrl() {
    String osName = System.getProperty("os.name").toLowerCase();
    String osArch = System.getProperty("os.arch").toLowerCase();

    String jarFilename = getJarFilename(osName, osArch);
    String pluginsPath = PathManager.getPluginsPath();
    String swtJarPath = pluginsPath + "/de.fu_berlin.inf.dpp.swt_plugin/resources/" + jarFilename;
    File jarFile = new File(swtJarPath);
    if (!jarFile.exists()) {
      throw new IllegalStateException(
          jarFile.getAbsolutePath()
              + " could not be found. This means the swt plugin is not installed correctly.");
    }
    URL swtFileUrl;
    try {
      swtFileUrl = jarFile.toURI().toURL();
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
    return swtFileUrl;
  }

  static String getJarFilename(String osName, String osArch) {
    return JAR_BASE_FILENAME + getOsSuffix(osName) + "-" + getArchSuffix(osArch) + ".jar";
  }

  private static String getArchSuffix(String osArch) {
    if (osArch.contains("64")) {
      return "x86_64";
    } else if (osArch.contains("86")) {
      return "x86";
    } else {
      throw new IllegalStateException("Your OS architecture: " + osArch + " is not supported.");
    }
  }

  private static String getOsSuffix(String osName) {
    if (osName.contains("win")) {
      return "win32";
    } else if (osName.contains("linux")) {
      return "gtk-linux";
    } else if (osName.contains("mac")) {
      return "cocoa-macosx";
    } else {
      throw new IllegalStateException("Your operating system: " + osName + " is not supported.");
    }
  }
}
