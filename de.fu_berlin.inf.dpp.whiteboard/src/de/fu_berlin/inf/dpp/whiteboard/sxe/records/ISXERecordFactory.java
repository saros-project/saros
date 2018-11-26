package de.fu_berlin.inf.dpp.whiteboard.sxe.records;

import de.fu_berlin.inf.dpp.whiteboard.sxe.SXEController;

/**
 * Use this factory to customize the IRecord implementations respective namespace, name and maybe
 * chdata.
 *
 * @author jurke
 */
public interface ISXERecordFactory {

  public abstract ElementRecord createElementRecord(
      DocumentRecord document, String ns, String name);

  public abstract AttributeRecord createAttributeRecord(
      DocumentRecord document, String ns, String name, String chdata);

  public abstract ElementRecord createRoot(DocumentRecord document);

  public abstract DocumentRecord createDocument(SXEController controller);
}
