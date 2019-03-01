package saros.whiteboard.sxe.net;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import saros.whiteboard.sxe.constants.RecordEntry;
import saros.whiteboard.sxe.records.serializable.RecordDataObject;

/**
 * Writer class for the SXE protocol (Shared XML Editing XEP-0284). Creates well-formed XML-Strings
 * from a SXEMessages.
 *
 * @author jurke
 */
public class SXEMessageWriter {

  private static final Logger log = Logger.getLogger(SXEMessageWriter.class);

  private SXEStreamWriter writer;
  private ByteArrayOutputStream os;

  public SXEMessageWriter(SXEStreamWriter writer) {
    try {
      this.writer = writer;
      this.os = new ByteArrayOutputStream();
      writer.initOutputStream(os);
    } catch (TransformerConfigurationException e) {
      throw new RuntimeException(e);
    }
  }

  public SXEMessageWriter() {
    this(new SXEStreamWriter());
  }

  public String getSXEMessageAsString(SXEMessage msg) {
    return getSXEMessageAsString(msg, msg.getRecords());
  }

  public String getSXEMessageAsString(SXEMessage msg, List<RecordDataObject> rdos) {
    try {

      writer.startMessage(msg);
      switch (msg.getMessageType()) {
        case ACCEPT_STATE:
          writer.writeAcceptState();
          break;
        case RECORDS:
          writer.writeRecords(rdos);
          break;
        case STATE:
          writer.writeState(rdos);
          break;
        case STATE_OFFER:
          writer.writeStateOffer();
          break;
      }
      writer.endMessage();
      String raw = new String(os.toByteArray(), "UTF-8");
      os.reset();
      return raw;

    } catch (SAXException e) {
      log.error("", e);
      throw new RuntimeException(e);
    } catch (UnsupportedEncodingException e) {
      log.error("", e);
      throw new RuntimeException(e);
    }
  }

  /**
   * The writer uses a SAX writer to produce well-formed XML respective to SXE
   *
   * @author jurke
   */
  public static class SXEStreamWriter {

    protected OutputStream stream;

    protected TransformerHandler handler;

    public SXEStreamWriter() {}

    public void initOutputStream(OutputStream os) throws TransformerConfigurationException {
      closeOutputStream();
      stream = os;
      StreamResult streamResult = new StreamResult(stream);
      SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
      handler = tf.newTransformerHandler();
      Transformer serializer = handler.getTransformer();
      serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      serializer.setOutputProperty(OutputKeys.METHOD, "xml");
      serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      handler.setResult(streamResult);
    }

    public void closeOutputStream() {
      if (stream != null) {
        try {
          stream.flush();
          stream.close();
        } catch (Exception e) {
          // nothing
        }
      }
    }

    public void startMessage(SXEMessage session) throws SAXException {

      AttributesImpl atts = new AttributesImpl();
      atts.addAttribute("", "", "id", "CDATA", session.getMessageId());
      atts.addAttribute("", "", "session", "CDATA", session.getSession().getSessionId());
      atts.addAttribute("", "", "xmlns", "CDATA", SXEMessage.SXE_XMLNS);

      handler.startElement("", "", SXEMessage.SXE_TAG, atts);
    }

    public void writeStateOffer() throws SAXException {
      handler.startElement("", "", "state-offer", null);

      AttributesImpl atts = new AttributesImpl();
      // TODO which description to use
      atts.addAttribute("", "", "xmlns", "CDATA", "urn:xmpp:apps:saros-whiteboard");
      handler.startElement("", "", "description", atts);
      handler.endElement("", "", "description");

      handler.endElement("", "", "state-offer");
    }

    public void writeAcceptState() throws SAXException {
      handler.startElement("", "", "accept-state", null);
      handler.endElement("", "", "accept-state");
    }

    public void writeRecords(List<RecordDataObject> rdos) throws SAXException {
      for (RecordDataObject record : rdos) {
        writeRecord(record);
      }
    }

    public void writeState(List<RecordDataObject> records) throws SAXException {
      handler.startElement("", "", "state", null);

      // TODO prolog
      handler.startElement("", "", "document-begin", null);
      handler.endElement("", "", "document-begin");

      for (RecordDataObject record : records) {
        writeRecord(record);
      }

      // TODO last-sender, last-id
      handler.startElement("", "", "document-end", null);
      handler.endElement("", "", "document-end");

      handler.endElement("", "", "state");
    }

    public void endMessage() throws SAXException {
      handler.endElement("", "", "sxe");
      handler.endDocument();
    }

    protected void writeRecord(RecordDataObject rdo) throws SAXException {
      handler.startElement(
          "", "", rdo.getRecordType().toString(), getAttributes(rdo.getValuePairs()));
      handler.endElement("", "", rdo.getRecordType().toString());
    }

    public Attributes getAttributes(Map<RecordEntry, String> valuePairs) {
      AttributesImpl atts = new AttributesImpl();
      for (Entry<RecordEntry, String> e : valuePairs.entrySet()) {
        atts.addAttribute("", "", e.getKey().toString(), "CDATA", e.getValue());
      }
      return atts;
    }
  }
}
