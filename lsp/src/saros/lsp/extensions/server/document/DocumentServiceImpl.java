package saros.lsp.extensions.server.document;

import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import saros.session.AbstractActivityProducer;

/** Empty implementation of the text document service. */
public class DocumentServiceImpl extends AbstractActivityProducer implements IDocumentService {

  @Override
  public void didChange(DidChangeTextDocumentParams arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void didClose(DidCloseTextDocumentParams arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void didOpen(DidOpenTextDocumentParams arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void didSave(DidSaveTextDocumentParams arg0) {
    // TODO Auto-generated method stub

  }
}
