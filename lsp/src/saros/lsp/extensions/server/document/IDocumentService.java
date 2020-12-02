package saros.lsp.extensions.server.document;

import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import org.eclipse.lsp4j.services.TextDocumentService;

@JsonSegment("textDocument")
public interface IDocumentService extends TextDocumentService {}
