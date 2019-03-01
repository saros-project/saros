package saros.whiteboard.sxe;

import saros.whiteboard.sxe.records.DocumentRecord;
import saros.whiteboard.sxe.records.ElementRecord;
import saros.whiteboard.sxe.records.ISXERecordFactory;

public class TestUtils {

  public static DocumentRecord getEmptyDocument(ISXERecordFactory recordFactory) {

    SXEController controller = new SXEController(recordFactory);
    DocumentRecord document = recordFactory.createDocument(controller);
    ElementRecord root = recordFactory.createRoot(document);
    root.apply(document);

    return document;
  }
}
