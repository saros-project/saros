package de.fu_berlin.inf.dpp.server;

/**
 * A facade for accessing the configuration properties that were passed to the Saros server on
 * startup.
 */
public class ServerConfig {

  private static final String JID_KEY = "de.fu_berlin.inf.dpp.server.jid";
  private static final String PASSWORD_KEY = "de.fu_berlin.inf.dpp.server.password";
  private static final String WORKSPACE_PATH_KEY = "de.fu_berlin.inf.dpp.server.workspace";
  private static final String INTERACTIVE_KEY = "de.fu_berlin.inf.dpp.server.interactive";

  /**
   * Returns the JID that the Saros server should use to connect to the XMPP network.
   *
   * @return JID to use, or <code>null</code> if not specified
   */
  public static final String getJID() {
    return System.getProperty(JID_KEY);
  }

  /**
   * Returns the password that the Saros server should use to authenticate with the XMPP server.
   *
   * @return password to use, or <code>null</code> if not specified
   */
  public static final String getPassword() {
    return System.getProperty(PASSWORD_KEY);
  }

  /**
   * Returns the path of the directory to use as the server's workspace. May be <code>null</code>,
   * which means the server should create a temporary workspace directory by itself and delete it on
   * exit.
   *
   * @return the path of the workspace directory to use, or <code>null</code> if a temporary
   *     directory should be used
   */
  public static String getWorkspacePath() {
    return System.getProperty(WORKSPACE_PATH_KEY);
  }

  /**
   * Returns if the user has requested an interactive console.
   *
   * @return if the user has requested an interactive console
   */
  public static boolean isInteractive() {
    String value = System.getProperty(INTERACTIVE_KEY);
    return value.equalsIgnoreCase("true")
        || value.equalsIgnoreCase("yes")
        || value.equalsIgnoreCase("y");
  }
}
