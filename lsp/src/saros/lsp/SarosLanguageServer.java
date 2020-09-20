package saros.lsp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.services.WorkspaceService;
import saros.lsp.extensions.server.ISarosLanguageServer;
import saros.lsp.extensions.server.account.IAccountService;
import saros.lsp.extensions.server.connection.IConnectionService;
import saros.lsp.extensions.server.contact.IContactService;
import saros.lsp.extensions.server.document.IDocumentService;

/** Implmenentation of {@link ISarosLanguageServer}. */
public class SarosLanguageServer implements ISarosLanguageServer {

  private IAccountService accountService;

  private IContactService contactService;

  private IDocumentService documentService;

  private WorkspaceService workspaceService;

  private IConnectionService connectionService;

  public SarosLanguageServer(
      IAccountService accountService,
      IContactService contactService,
      IDocumentService documentService,
      IConnectionService connectionService,
      WorkspaceService workspaceService) {
    this.accountService = accountService;
    this.contactService = contactService;
    this.documentService = documentService;
    this.connectionService = connectionService;
    this.workspaceService = workspaceService;
  }

  @Override
  public CompletableFuture<InitializeResult> initialize(InitializeParams params) {

    this.initializeListeners.forEach(listener -> listener.accept(params));

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

    capabilities.setExperimental(true);
    capabilities.setTextDocumentSync(TextDocumentSyncKind.Incremental);

    return capabilities;
  }

  @Override
  public CompletableFuture<Object> shutdown() {
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public void exit() {
    this.exitListeners.forEach(listener -> listener.run());
  }

  private List<Runnable> exitListeners = new ArrayList<>();
  private List<Consumer<InitializeParams>> initializeListeners = new ArrayList<>();

  @Override
  public void onInitialize(Consumer<InitializeParams> consumer) {
    this.initializeListeners.add(consumer);
  }

  @Override
  public void onExit(Runnable runnable) {
    this.exitListeners.add(runnable);
  }

  @Override
  public IDocumentService getTextDocumentService() {
    return this.documentService;
  }

  @Override
  public WorkspaceService getWorkspaceService() {
    return this.workspaceService;
  }

  @Override
  public IAccountService getSarosAccountService() {
    return this.accountService;
  }

  @Override
  public IContactService getSarosContactService() {
    return this.contactService;
  }

  @Override
  public IConnectionService getSarosConnectionService() {
    return this.connectionService;
  }
}
