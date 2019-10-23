package saros.lsp;

import java.util.concurrent.CompletableFuture;
import org.apache.log4j.Logger;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import saros.lsp.extensions.ISarosLanguageServer;
import saros.lsp.extensions.client.ISarosLanguageClient;
import saros.lsp.extensions.client.ISarosLanguageClientAware;
import saros.lsp.extensions.server.account.AccountService;
import saros.lsp.extensions.server.account.IAccountService;
import saros.lsp.service.DocumentServiceStub;
import saros.lsp.service.WorkspaceServiceStub;

/** Implmenentation of the Saros language server. */
public class SarosLanguageServer implements ISarosLanguageServer, ISarosLanguageClientAware {

  private static final Logger LOG = Logger.getLogger(SarosLauncher.class);

  private ISarosLanguageClient languageClient;

  @Override
  public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
    return CompletableFuture.completedFuture(new InitializeResult(this.createCapabilities()));
  }

  /**
   * Creates the capabilities of the server.
   *
   * @return ServerCapabilities capabilities of the server
   */
  private ServerCapabilities createCapabilities() {
    ServerCapabilities capabilities = new ServerCapabilities();

    return capabilities;
  }

  @Override
  public CompletableFuture<Object> shutdown() {
    LOG.info("shutdown");
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public void exit() {
    LOG.info("exit");
  }

  @Override
  public TextDocumentService getTextDocumentService() {
    return new DocumentServiceStub();
  }

  @Override
  public WorkspaceService getWorkspaceService() {
    return new WorkspaceServiceStub();
  }

  @Override
  public void connect(ISarosLanguageClient client) {
    this.languageClient = client;
  }

  @Override
  public IAccountService getSarosAccountService() {
    return new AccountService();
  }
}
