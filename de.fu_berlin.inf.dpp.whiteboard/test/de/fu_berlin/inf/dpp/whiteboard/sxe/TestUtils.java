package de.fu_berlin.inf.dpp.whiteboard.sxe;

import de.fu_berlin.inf.dpp.whiteboard.sxe.records.DocumentRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.ElementRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.ISXERecordFactory;

public class TestUtils {

  public static DocumentRecord getEmptyDocument(ISXERecordFactory recordFactory) {

    SXEController controller = new SXEController(recordFactory);
    DocumentRecord document = recordFactory.createDocument(controller);
    ElementRecord root = recordFactory.createRoot(document);
    root.apply(document);

    return document;
  }
}
