package saros.lsp.context;

import org.eclipse.lsp4j.services.WorkspaceService;
import saros.context.AbstractContextFactory;
import saros.lsp.SarosLanguageServer;
import saros.lsp.extensions.server.ISarosLanguageServer;
import saros.lsp.extensions.server.account.AccountService;
import saros.lsp.extensions.server.account.IAccountService;
import saros.lsp.extensions.server.connection.ConnectionService;
import saros.lsp.extensions.server.connection.IConnectionService;
import saros.lsp.extensions.server.contact.ContactService;
import saros.lsp.extensions.server.contact.IContactService;
import saros.lsp.extensions.server.document.DocumentServiceImpl;
import saros.lsp.extensions.server.document.IDocumentService;
import saros.lsp.extensions.server.workspace.WorkspaceServiceImpl;
import saros.repackaged.picocontainer.MutablePicoContainer;

/** ContextFactory for all Saros language server services. */
public class LspContextFactory extends AbstractContextFactory {

  @Override
  public void createComponents(MutablePicoContainer container) {
    container.addComponent(ISarosLanguageServer.class, SarosLanguageServer.class);
    container.addComponent(IAccountService.class, AccountService.class);
    container.addComponent(IContactService.class, ContactService.class);
    container.addComponent(IConnectionService.class, ConnectionService.class);
    container.addComponent(IDocumentService.class, DocumentServiceImpl.class);
    container.addComponent(WorkspaceService.class, WorkspaceServiceImpl.class);
  }
}
