package saros.lsp.extensions.server;

import java.util.function.Consumer;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.jsonrpc.services.JsonDelegate;
import org.eclipse.lsp4j.services.LanguageServer;
import saros.lsp.extensions.server.account.IAccountService;
import saros.lsp.extensions.server.connection.IConnectionService;
import saros.lsp.extensions.server.contact.IContactService;
import saros.lsp.extensions.server.document.IDocumentService;

/**
 * Interface of the Saros language server.
 *
 * <p>It defines which services are available to be consumed.
 *
 * <p>All Saros related features that aren't covered by the lsp protocol have to be specified here.
 */
public interface ISarosLanguageServer extends LanguageServer {

  /** Provides access to the account services. */
  @JsonDelegate
  IAccountService getSarosAccountService();

  /** Provides access to the contact services. */
  @JsonDelegate
  IContactService getSarosContactService();

  /** Provides access to the connection services. */
  @JsonDelegate
  IConnectionService getSarosConnectionService();

  /**
   * Registers a {@link Runnable} that will be executed when the server exits.
   *
   * @param runnable {@link Runnable} to execute upon exit
   */
  void onExit(Runnable runnable);

  /**
   * Registers a {@link Consumer} that will be executed when the server initializes.
   *
   * @param consumer {@link Consumer} that takes the {@link InitializeParams} and will be executed
   *     upon initialization.
   */
  void onInitialize(Consumer<InitializeParams> consumer);

  @Override
  @JsonDelegate
  IDocumentService getTextDocumentService();
}
