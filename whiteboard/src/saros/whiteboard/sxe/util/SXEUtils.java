package saros.whiteboard.sxe.util;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.log4j.Logger;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import saros.whiteboard.sxe.records.AttributeRecord;
import saros.whiteboard.sxe.records.ElementRecord;
import saros.whiteboard.sxe.records.IRecord;
import saros.whiteboard.sxe.records.serializable.RecordDataObject;

public class SXEUtils {

  private static final Logger log = Logger.getLogger(SXEUtils.class);

  private static DOMImplementation domImplementation = null;

  public static DOMImplementation getDOMImplementation() {
    if (domImplementation == null)
      try {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        domImplementation = builder.getDOMImplementation();
        return domImplementation;
      } catch (ParserConfigurationException e) {
        // shouldn't happen, however should not return null
        throw new RuntimeException(e);
      }
    return domImplementation;
  }

  public static Document getEmptyDocument(String ns, String name) {
    Document d = getDOMImplementation().createDocument(ns, name, null);
    return d;
  }

  /**
   * @param record root record of subtree
   * @return the SXE tree as XML String
   */
  public static String toXmlString(ElementRecord record) {

    Document document = toDocument(record);

    Transformer transformer;
    try {

      transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      // initialize StreamResult with File object to save to file
      StreamResult result = new StreamResult(new StringWriter());
      DOMSource source = new DOMSource(document);
      transformer.transform(source, result);
      return result.getWriter().toString();

    } catch (Exception e) {
      log.warn("Could not create string from document: " + e.getMessage());
      return "";
    }
  }

  /**
   * @param rootRecord
   * @return the SXE tree as W3C DOM document
   */
  public static Document toDocument(ElementRecord rootRecord) {

    Document document = getEmptyDocument(rootRecord.getNs(), rootRecord.getName());
    Element root = document.getDocumentElement();
    root.setAttributeNS(null, "rid", rootRecord.getRid());

    addChildNodes(rootRecord, root);

    return document;
  }

  /**
   * Helper method to add all AttributeRecords as DOM attribute
   *
   * @param attributes
   * @param element to add the attributes to
   */
  protected static void addAttributes(List<AttributeRecord> attributes, Element parent) {
    for (AttributeRecord record : attributes) {
      parent.setAttributeNS(record.getNs(), record.getName(), record.getChdata());
    }
  }

  /**
   * Helper method to add all child element records as DOM child elements
   *
   * @param elements
   * @param element to add the elements to
   */
  protected static void addElements(List<ElementRecord> elements, Element parent) {
    Element e;
    for (ElementRecord record : elements) {
      e = parent.getOwnerDocument().createElementNS(record.getNs(), record.getName());
      // TODO remove RID
      e.setAttributeNS(null, "rid", record.getRid());
      parent.appendChild(e);
      addChildNodes(record, e);
    }
  }

  /**
   * Helper method to add all child nodes as DOM nodes to element
   *
   * @param record
   * @param element to add the nodes to
   */
  protected static void addChildNodes(ElementRecord record, Element element) {
    addElements(record.getVisibleChildElements(), element);
    addAttributes(record.getVisibleAttributes(), element);
  }

  /**
   * @param list of IRecords
   * @return the IRecords as RecordDataObjects
   */
  public static List<RecordDataObject> toDataObjects(List<IRecord> records) {
    List<RecordDataObject> rdos = new ArrayList<RecordDataObject>(records.size());

    for (IRecord r : records) {
      rdos.add(r.getRecordDataObject());
    }
    return rdos;
  }
}
