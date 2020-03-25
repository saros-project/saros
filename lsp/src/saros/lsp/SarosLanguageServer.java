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

  private static final Logger log = Logger.getLogger(SarosLanguageServer.class);

  private ISarosLanguageClient languageClient;

  @Override
  public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
    return CompletableFuture.completedFuture(new InitializeResult(this.createCapabilities()));
  }

  /**
   * Creates the language capabilities of the server.
   *
   * <p>Capabilities are language related features like: * syntax highlighting * code lens * hover *
   * code completition
   *
   * <p>The capabilities are being evaluated by the IDE that uses the server in order to know which
   * features can be used.
   *
   * <p>Since this server isn't processing any programming language in the original sense all
   * features will default to false.
   *
   * @return ServerCapabilities capabilities of the server
   */
  private ServerCapabilities createCapabilities() {
    ServerCapabilities capabilities = new ServerCapabilities();

    return capabilities;
  }

  @Override
  public CompletableFuture<Object> shutdown() {
    log.info("shutdown");
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public void exit() {
    log.info("exit");
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
