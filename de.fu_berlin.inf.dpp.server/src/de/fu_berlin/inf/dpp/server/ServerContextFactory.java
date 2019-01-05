package de.fu_berlin.inf.dpp.server;

import de.fu_berlin.inf.dpp.communication.connection.IProxyResolver;
import de.fu_berlin.inf.dpp.communication.connection.NullProxyResolver;
import de.fu_berlin.inf.dpp.context.AbstractContextFactory;
import de.fu_berlin.inf.dpp.context.IContextKeyBindings;
import de.fu_berlin.inf.dpp.filesystem.IChecksumCache;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IPathFactory;
import de.fu_berlin.inf.dpp.filesystem.IWorkspace;
import de.fu_berlin.inf.dpp.filesystem.NullChecksumCache;
import de.fu_berlin.inf.dpp.monitoring.remote.IRemoteProgressIndicatorFactory;
import de.fu_berlin.inf.dpp.preferences.IPreferenceStore;
import de.fu_berlin.inf.dpp.preferences.Preferences;
import de.fu_berlin.inf.dpp.server.console.InviteCommand;
import de.fu_berlin.inf.dpp.server.console.ServerConsole;
import de.fu_berlin.inf.dpp.server.console.ShareCommand;
import de.fu_berlin.inf.dpp.server.dummies.NullRemoteProgressIndicatorFactory;
import de.fu_berlin.inf.dpp.server.filesystem.ServerPathFactoryImpl;
import de.fu_berlin.inf.dpp.server.filesystem.ServerPathImpl;
import de.fu_berlin.inf.dpp.server.filesystem.ServerReferencePointManager;
import de.fu_berlin.inf.dpp.server.filesystem.ServerWorkspaceImpl;
import de.fu_berlin.inf.dpp.server.net.SubscriptionAuthorizer;
import de.fu_berlin.inf.dpp.server.preferences.PersistencePreferenceStore;
import de.fu_berlin.inf.dpp.server.preferences.ServerPreferences;
import de.fu_berlin.inf.dpp.server.session.JoinSessionRequestHandler;
import de.fu_berlin.inf.dpp.server.session.NegotiationHandler;
import de.fu_berlin.inf.dpp.server.session.ServerSessionContextFactory;
import de.fu_berlin.inf.dpp.server.synchronize.ServerUISynchronizerImpl;
import de.fu_berlin.inf.dpp.session.ISarosSessionContextFactory;
import de.fu_berlin.inf.dpp.synchronize.UISynchronizer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.picocontainer.BindKey;
import org.picocontainer.MutablePicoContainer;

/**
 * Server implementation of {@link de.fu_berlin.inf.dpp.context.IContextFactory}. In addition to the
 * core components configured in {@link de.fu_berlin.inf.dpp.context.CoreContextFactory}, this class
 * adds the server-specific components such as implementations of unimplemented core interfaces.
 */
public class ServerContextFactory extends AbstractContextFactory {

  private static final Logger LOG = Logger.getLogger(ServerContextFactory.class);

  @Override
  public void createComponents(MutablePicoContainer c) {
    addVersionString(c);
    addCoreInterfaceImplementations(c);
    addOptionalCoreInterfaceImplementations(c);
    addAdditionalComponents(c);
  }

  private void addVersionString(MutablePicoContainer c) {
    c.addComponent(
        BindKey.bindKey(String.class, IContextKeyBindings.SarosVersion.class),
        SarosServer.SAROS_VERSION);
  }

  private void addCoreInterfaceImplementations(MutablePicoContainer c) {
    // File System
    c.addComponent(IPathFactory.class, ServerPathFactoryImpl.class);
    c.addComponent(IWorkspace.class, createWorkspace());

    // Preferences
    c.addComponent(IPreferenceStore.class, PersistencePreferenceStore.class);
    c.addComponent(Preferences.class, ServerPreferences.class);

    // Session
    c.addComponent(ISarosSessionContextFactory.class, ServerSessionContextFactory.class);

    // Other
    c.addComponent(IRemoteProgressIndicatorFactory.class, NullRemoteProgressIndicatorFactory.class);

    c.addComponent(UISynchronizer.class, ServerUISynchronizerImpl.class);

    c.addComponent(ServerReferencePointManager.class);
  }

  /*
   * Components that are not necessarily needed but must be present, i.e. use
   * dummies
   */
  private void addOptionalCoreInterfaceImplementations(MutablePicoContainer c) {
    c.addComponent(IProxyResolver.class, NullProxyResolver.class);
    c.addComponent(IChecksumCache.class, NullChecksumCache.class);
  }

  private void addAdditionalComponents(MutablePicoContainer c) {
    c.addComponent(SubscriptionAuthorizer.class);
    c.addComponent(NegotiationHandler.class);
    c.addComponent(JoinSessionRequestHandler.class);
    if (ServerConfig.isInteractive()) {
      c.addComponent(new ServerConsole(System.in, System.out));
      c.addComponent(InviteCommand.class);
      c.addComponent(ShareCommand.class);
    }
  }

  private IWorkspace createWorkspace() {
    String pathString = ServerConfig.getWorkspacePath();

    if (pathString == null) {
      pathString = createTemporaryWorkspaceFolder();
    }

    IPath location = ServerPathImpl.fromString(pathString);
    return new ServerWorkspaceImpl(location);
  }

  private String createTemporaryWorkspaceFolder() {
    final Path folderPath;

    try {
      folderPath = Files.createTempDirectory("saros-server-workspace");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    deleteWorkspaceFolderOnShutdown(folderPath);
    return folderPath.toString();
  }

  // FIXME shutdown hooks are not the best option to do this
  private void deleteWorkspaceFolderOnShutdown(final Path path) {
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread() {
              @Override
              public void run() {
                try {
                  FileUtils.deleteDirectory(path.toFile());
                } catch (IOException e) {
                  LOG.warn("Could not remove temporary workspace folder", e);
                }
              }
            });
  }
}
