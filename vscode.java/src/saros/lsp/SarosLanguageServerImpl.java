package saros.lsp;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

import saros.lsp.extensions.SarosLanguageServer;
import saros.lsp.extensions.client.SarosLanguageClient;
import saros.lsp.extensions.client.SarosLanguageClientAware;
import saros.lsp.extensions.server.account.SarosAccountService;
import saros.lsp.extensions.server.account.SarosAccountServiceImpl;
import saros.lsp.service.SarosDocumentService;
import saros.lsp.service.SarosWorkspaceService;

public class SarosLanguageServerImpl implements SarosLanguageServer, SarosLanguageClientAware {

    private SarosLanguageClient languageClient;

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        return CompletableFuture.completedFuture(new InitializeResult(this.getCapabilities()));
    }

    private ServerCapabilities getCapabilities() {
        ServerCapabilities capabilities = new ServerCapabilities();

        return capabilities;
    }

    @Override
    public CompletableFuture<Object> shutdown() {
        // System.out.println("shutdown");
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void exit() {
        // System.out.println("exit");
    }

    @Override
    public TextDocumentService getTextDocumentService() {
        return new SarosDocumentService();
    }

    @Override
    public WorkspaceService getWorkspaceService() {
        return new SarosWorkspaceService();
    }

    @Override
    public void connect(SarosLanguageClient client) {
        this.languageClient = client;
    }

    @Override
    public SarosAccountService getSarosAccountService() {
        return new SarosAccountServiceImpl();
    }

}