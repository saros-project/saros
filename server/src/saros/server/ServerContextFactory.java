package saros.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.picocontainer.BindKey;
import org.picocontainer.MutablePicoContainer;
import saros.communication.connection.IProxyResolver;
import saros.communication.connection.NullProxyResolver;
import saros.context.AbstractContextFactory;
import saros.context.IContextKeyBindings;
import saros.filesystem.IChecksumCache;
import saros.filesystem.IPath;
import saros.filesystem.IPathFactory;
import saros.filesystem.IWorkspace;
import saros.filesystem.NullChecksumCache;
import saros.monitoring.remote.IRemoteProgressIndicatorFactory;
import saros.preferences.IPreferenceStore;
import saros.preferences.Preferences;
import saros.server.console.InviteCommand;
import saros.server.console.ServerConsole;
import saros.server.console.ShareCommand;
import saros.server.dummies.NullRemoteProgressIndicatorFactory;
import saros.server.filesystem.ServerPathFactoryImpl;
import saros.server.filesystem.ServerPathImpl;
import saros.server.filesystem.ServerWorkspaceImpl;
import saros.server.net.SubscriptionAuthorizer;
import saros.server.preferences.PersistencePreferenceStore;
import saros.server.preferences.ServerPreferences;
import saros.server.session.JoinSessionRequestHandler;
import saros.server.session.NegotiationHandler;
import saros.server.session.ServerSessionContextFactory;
import saros.server.synchronize.ServerUISynchronizerImpl;
import saros.session.ISarosSessionContextFactory;
import saros.synchronize.UISynchronizer;

/**
 * Server implementation of {@link saros.context.IContextFactory}. In addition to the core
 * components configured in {@link saros.context.CoreContextFactory}, this class adds the
 * server-specific components such as implementations of unimplemented core interfaces.
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
