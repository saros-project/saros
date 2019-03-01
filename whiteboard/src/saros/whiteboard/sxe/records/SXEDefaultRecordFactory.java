package saros.whiteboard.sxe.records;

import saros.whiteboard.sxe.SXEController;

/**
 * Default factory implementation that returns standard SXE instances.
 *
 * @author jurke
 */
public class SXEDefaultRecordFactory implements ISXERecordFactory {

  /*
   * (non-Javadoc)
   *
   * @see saros.whiteboard.sxe.records.ISXERecordFactory#
   * createElementRecord
   * (saros.whiteboard.sxe.records.DocumentRecord,
   * java.lang.String, java.lang.String)
   */
  @Override
  public ElementRecord createElementRecord(DocumentRecord document, String ns, String name) {
    return new ElementRecord(document);
  }

  /*
   * (non-Javadoc)
   *
   * @see saros.whiteboard.sxe.records.ISXERecordFactory#
   * createAttributeRecord
   * (saros.whiteboard.sxe.records.DocumentRecord,
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
   * saros.whiteboard.sxe.records.ISXERecordFactory#createRoot
   * (saros.whiteboard.sxe.records.DocumentRecord)
   */
  @Override
  public ElementRecord createRoot(DocumentRecord document) {
    return new ElementRecord(document);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * saros.whiteboard.sxe.records.ISXERecordFactory#createDocument
   * (saros.whiteboard.sxe.ISXEController)
   */
  @Override
  public DocumentRecord createDocument(SXEController controller) {
    return new DocumentRecord(controller);
  }
}
