package saros.lsp.extensions;

import org.eclipse.lsp4j.jsonrpc.services.JsonDelegate;
import org.eclipse.lsp4j.services.LanguageServer;
import saros.lsp.extensions.server.account.IAccountService;

/** Interface of the Saros language server. */
public interface ISarosLanguageServer extends LanguageServer {

  /** Provides access to the account services. */
  @JsonDelegate
  IAccountService getSarosAccountService();
}
