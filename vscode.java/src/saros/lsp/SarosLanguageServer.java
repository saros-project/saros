package saros.lsp;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

import saros.lsp.service.SarosDocumentService;
import saros.lsp.service.SarosWorkspaceService;

public class SarosLanguageServer implements LanguageServer, LanguageClientAware {

    private LanguageClient languageClient;

    public void sendHello() {
                
        MessageParams mp = new MessageParams();
        mp.setMessage("connected");
        mp.setType(MessageType.Info);
        
        this.languageClient.showMessage(mp);

        this.languageClient.logMessage(mp);
    }

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
        //System.out.println("shutdown");
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void exit() {
        //System.out.println("exit");
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
    public void connect(LanguageClient client) {
        this.languageClient = client;
    }

}