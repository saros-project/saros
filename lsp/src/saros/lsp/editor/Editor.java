package saros.lsp.editor;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.eclipse.lsp4j.TextDocumentItem;
import saros.filesystem.IFile;

public class Editor extends TextDocumentItem {

  public Editor(TextDocumentItem documentItem) {
    this(documentItem.getText(), documentItem.getUri());
    super.setVersion(documentItem.getVersion());
    super.setLanguageId(documentItem.getLanguageId());
  }

  public Editor(String text, String uri) {
    super.setUri(uri);
    super.setText(text);
  }

  public Editor(IFile file) throws IOException {
    super.setUri("file:///" + file.getReferencePointRelativePath().toString());

    try (InputStream stream = file.getContents()) {
      super.setText(IOUtils.toString(stream));
    }
  }
}
