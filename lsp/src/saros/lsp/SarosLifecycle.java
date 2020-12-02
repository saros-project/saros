package saros.lsp;

import java.util.ArrayList;
import java.util.Collection;
import saros.context.AbstractContextLifecycle;
import saros.context.IContextFactory;
import saros.lsp.context.CoreContextFactory;
import saros.lsp.context.FileSystemContextFactory;
import saros.lsp.context.LspContextFactory;
import saros.lsp.context.ProxyContextFactory;
import saros.lsp.context.UIContextFactory;
import saros.lsp.extensions.client.ISarosLanguageClient;
import saros.lsp.extensions.server.ISarosLanguageServer;
import saros.lsp.filesystem.IWorkspacePath;

/** The lifecycle of the Saros language server. */
public class SarosLifecycle extends AbstractContextLifecycle {

  private ISarosLanguageClient client;
  private IWorkspacePath workspace;

  @Override
  protected Collection<IContextFactory> additionalContextFactories() {
    Collection<IContextFactory> factories = new ArrayList<IContextFactory>();

    factories.add(new CoreContextFactory());
    factories.add(new LspContextFactory());
    factories.add(new UIContextFactory());
    factories.add(new FileSystemContextFactory());
    factories.add(
        new ProxyContextFactory<ISarosLanguageClient>(
            ISarosLanguageClient.class, () -> this.client));
    factories.add(
        new ProxyContextFactory<IWorkspacePath>(IWorkspacePath.class, () -> this.workspace));

    return factories;
  }

  /**
   * Gets the instance of the Saros language server.
   *
   * @return The Saros language server
   */
  public ISarosLanguageServer getLanguageServer() {
    return this.getSarosContext().getComponent(ISarosLanguageServer.class);
  }

  /**
   * Registers the used Saros language client instance.
   *
   * @param client The Saros language client that is connected
   * @return The registered Saros language client
   */
  public ISarosLanguageClient registerLanguageClient(ISarosLanguageClient client) {

    this.client = client;

    return client;
  }

  /**
   * Registers the workspace path the Saros language server is responsible for.
   *
   * @param workspace The used workspace
   * @return The registered workspace path
   */
  public IWorkspacePath registerWorkspace(IWorkspacePath workspace) {

    this.workspace = workspace;

    return workspace;
  }
}
