package de.fu_berlin.inf.dpp.whiteboard.sxe.records;

import de.fu_berlin.inf.dpp.whiteboard.sxe.SXEController;

/**
 * Default factory implementation that returns standard SXE instances.
 *
 * @author jurke
 */
public class SXEDefaultRecordFactory implements ISXERecordFactory {

  /*
   * (non-Javadoc)
   *
   * @see de.fu_berlin.inf.dpp.whiteboard.sxe.records.ISXERecordFactory#
   * createElementRecord
   * (de.fu_berlin.inf.dpp.whiteboard.sxe.records.DocumentRecord,
   * java.lang.String, java.lang.String)
   */
  @Override
  public ElementRecord createElementRecord(DocumentRecord document, String ns, String name) {
    return new ElementRecord(document);
  }

  /*
   * (non-Javadoc)
   *
   * @see de.fu_berlin.inf.dpp.whiteboard.sxe.records.ISXERecordFactory#
   * createAttributeRecord
   * (de.fu_berlin.inf.dpp.whiteboard.sxe.records.DocumentRecord,
   * java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public AttributeRecord createAttributeRecord(
      DocumentRecord document, String ns, String name, String chdata) {
    return new AttributeRecord(document);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * de.fu_berlin.inf.dpp.whiteboard.sxe.records.ISXERecordFactory#createRoot
   * (de.fu_berlin.inf.dpp.whiteboard.sxe.records.DocumentRecord)
   */
  @Override
  public ElementRecord createRoot(DocumentRecord document) {
    return new ElementRecord(document);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * de.fu_berlin.inf.dpp.whiteboard.sxe.records.ISXERecordFactory#createDocument
   * (de.fu_berlin.inf.dpp.whiteboard.sxe.ISXEController)
   */
  @Override
  public DocumentRecord createDocument(SXEController controller) {
    return new DocumentRecord(controller);
  }
}
