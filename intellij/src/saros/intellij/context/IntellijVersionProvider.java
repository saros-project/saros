package saros.intellij.context;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.extensions.PluginId;
import saros.intellij.SarosComponent;

/**
 * Methods in this class can be used to access version information about the running Intellij IDEA
 * version and Saros plugin version.
 */
public class IntellijVersionProvider {
  private IntellijVersionProvider() {
    // NOP
  }

  /**
   * Returns the version number of the used Saros plugin.
   *
   * @return the version number of the used Saros plugin
   */
  public static String getPluginVersion() {
    IdeaPluginDescriptor sarosPluginDescriptor =
        PluginManagerCore.getPlugin(PluginId.getId(SarosComponent.PLUGIN_ID));

    if (sarosPluginDescriptor == null) {
      throw new IllegalStateException(
          "No plugin with the id \"" + SarosComponent.PLUGIN_ID + "\" was found");
    }

    return sarosPluginDescriptor.getVersion();
  }

  /**
   * Returns the build number of the used Intellij IDEA version.
   *
   * <p><b>NOTE:</b> This is <b>not</b> the version number but the number of the build used for the
   * release.
   *
   * @return the build number of the used Intellij IDEA version
   */
  public static String getBuildNumber() {
    return ApplicationInfo.getInstance().getBuild().toString();
  }
}
