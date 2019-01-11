package de.fu_berlin.inf.dpp.intellij.context;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.extensions.PluginId;

/**
 * Methods in this class can be used to access version information about the running IntelliJ IDEA
 * version and Saros plugin version
 */
public class IntelliJVersionProvider {
  // plugin id set in plugin.xml with the tag <id>
  protected static final String PLUGIN_ID = "de.fu_berlin.inf.dpp.intellij";

  private IntelliJVersionProvider() {
    // NOP
  }

  /**
   * Returns the version number of the used Saros plugin.
   *
   * @return the version number of the used Saros plugin
   */
  public static String getPluginVersion() {
    IdeaPluginDescriptor sarosPluginDescriptor = PluginManager.getPlugin(PluginId.getId(PLUGIN_ID));

    if (sarosPluginDescriptor == null) {
      throw new IllegalStateException("No plugin with the id \"" + PLUGIN_ID + "\" was found");
    }

    return sarosPluginDescriptor.getVersion();
  }

  /**
   * Returns the build number of the used IntelliJ IDEA version.
   *
   * <p><b>NOTE:</b> This is <b>not</b> the version number but the number of the build used for the
   * release.
   *
   * @return the build number of the used IntelliJ IDEA version
   */
  public static String getBuildNumber() {
    return ApplicationInfo.getInstance().getBuild().toString();
  }
}
