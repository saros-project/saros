package saros.lsp;

import java.util.concurrent.CompletableFuture;

import org.apache.log4j.Logger;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

import saros.lsp.extensions.SarosLanguageServer;
import saros.lsp.extensions.client.SarosLanguageClient;
import saros.lsp.extensions.client.SarosLanguageClientAware;
import saros.lsp.extensions.server.account.AccountService;
import saros.lsp.extensions.server.account.AccountServiceImpl;
import saros.lsp.service.DocumentServiceImpl;
import saros.lsp.service.WorkspaceServiceImpl;

/**
 * Implmenentation of the Saros language server.
 */
public class SarosLanguageServerImpl implements SarosLanguageServer, SarosLanguageClientAware {

    private static final Logger LOG = Logger.getLogger(SarosLauncher.class);
    
    private SarosLanguageClient languageClient;
    
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
        return new DocumentServiceImpl();
    }

    @Override
    public WorkspaceService getWorkspaceService() {
        return new WorkspaceServiceImpl();
    }
    
    @Override
    public void connect(SarosLanguageClient client) {
        this.languageClient = client;
    }
    
    @Override
    public AccountService getSarosAccountService() {
        return new AccountServiceImpl();
    }

}