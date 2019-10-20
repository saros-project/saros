package saros.lsp.extensions;

import org.eclipse.lsp4j.jsonrpc.services.JsonDelegate;
import org.eclipse.lsp4j.services.LanguageServer;

import saros.lsp.extensions.server.account.SarosAccountService;

public interface SarosLanguageServer extends LanguageServer {
    @JsonDelegate
    SarosAccountService getSarosAccountService();
}