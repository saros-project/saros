package saros.gradle.intellij;

import java.io.File;

/**
 * Gradle extension that contains the configurable properties of the custom gradle plugin. The
 * provided setter and getters are used implicitly by gradle.
 *
 * <p>Example gradle configuration that:
 *
 * <pre>
 * sarosIntellij {
 *   sandboxBaseDir = System.getenv("SAROS_INTELLIJ_SANDBOX") as File
 *   localIntellijHome = System.getenv("INTELLIJ_HOME") as File
 *   intellijVersion = 'IC-2019.2.3'
 * }
 * </pre>
 */
public class SarosIntellijExtension {

  private File sandboxBaseDir;
  private File localIntellijHome;
  private String intellijVersion;
  private String pluginName = "Saros";

  /**
   * Set directory which contains the intellij sandbox directories.
   *
   * @param sandboxBaseDir The base directory which contains the sandbox dirs.
   */
  public void setSandboxBaseDir(File sandboxBaseDir) {
    this.sandboxBaseDir = sandboxBaseDir;
  }

  public File getSandboxBaseDir() {
    return sandboxBaseDir;
  }

  /**
   * Set directory of an intellij installation which will be used to execute the {@code runIde}
   * task.
   *
   * @param localIntellijHome Intellij installation directory
   */
  public void setLocalIntellijHome(File localIntellijHome) {
    this.localIntellijHome = localIntellijHome;
  }

  public File getLocalIntellijHome() {
    return localIntellijHome;
  }

  /**
   * Set the intellij version which is used to execute the {@code runIde} task. This version is
   * downloaded if no local installation is specified by {@code setLocalIntellijHome(File
   * localIntellijHome)}
   *
   * @param intellijVersion The intellij version e.g. "IC-2019.2.3"
   */
  public void setIntellijVersion(String intellijVersion) {
    this.intellijVersion = intellijVersion;
  }

  public String getIntellijVersion() {
    return intellijVersion;
  }

  /**
   * Set the plugin name which is used in the build plugin.
   *
   * @param pluginName Name of the Intellij IDEA plugin
   */
  public void setPluginName(String pluginName) {
    this.pluginName = pluginName;
  }

  public String getPluginName() {
    return pluginName;
  }
}
