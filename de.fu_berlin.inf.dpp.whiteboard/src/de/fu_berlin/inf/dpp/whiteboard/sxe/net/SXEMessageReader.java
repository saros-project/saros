package de.fu_berlin.inf.dpp.whiteboard.sxe.net;

import de.fu_berlin.inf.dpp.whiteboard.sxe.constants.RecordEntry;
import de.fu_berlin.inf.dpp.whiteboard.sxe.constants.RecordType;
import de.fu_berlin.inf.dpp.whiteboard.sxe.constants.SXEMessageType;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.serializable.NewRecordDataObject;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.serializable.RecordDataObject;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.serializable.SetRecordDataObject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import org.xmlpull.mxp1.MXParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Reader class for the SXE protocol. Reads SXEMessages from strings or pull parsers.
 *
 * @author jurke
 */
/*
 * Does not use static methods:
 *
 * - it is plugged in where applicable (i.e. a Smack Packet Extension)
 *
 * - should enable subclassing for customization
 */
public class SXEMessageReader {

  private static final Logger log = Logger.getLogger(SXEMessageReader.class);

  /*
   * factory method
   */
  protected RecordDataObject getRdo(RecordType t) {
    switch (t) {
      case NEW:
        return new NewRecordDataObject();
      case SET:
        return new SetRecordDataObject();
    }
    log.warn("Unknown record type: " + t);

    return null;
  }

  /*
   * just parses the attributes to the RecordDataObjext
   */
  protected void fillPairsfromAttributes(RecordDataObject rdo, XmlPullParser xpp) {
    for (int i = 0; i < xpp.getAttributeCount(); i++) {
      rdo.putValue(RecordEntry.fromString(xpp.getAttributeName(i)), xpp.getAttributeValue(i));
    }
  }

  /*
   * reads records (incrementing the xpp) until there are no more
   */
  protected List<RecordDataObject> getRecords(XmlPullParser xpp)
      throws XmlPullParserException, IOException {

    List<RecordDataObject> rdos = new LinkedList<RecordDataObject>();
    RecordType t = RecordType.fromString(xpp.getName());
    RecordDataObject rdo;

    while (t != null) {
      rdo = getRdo(t);
      fillPairsfromAttributes(rdo, xpp);
      rdos.add(rdo);

      while (xpp.next() != XmlPullParser.START_TAG) {
        if (xpp.getName().equals(SXEMessage.SXE_TAG)) // == END_TAG
        return rdos;
      }
      t = RecordType.fromString(xpp.getName());
    }

    return rdos;
  }

  /*
   * parses the SXEMessage attributes
   */
  protected SXEMessage getMessageInfo(XmlPullParser xpp) {
    String sessionId = xpp.getAttributeValue(null, "session");
    SXESession session = new SXESession(sessionId);
    String msgId = xpp.getAttributeValue(null, "id");
    return new SXEMessage(session, msgId);
  }

  protected boolean isRecord(String name) {
    return RecordType.fromString(name) != null;
  }

  public SXEMessage parseMessage(String raw) throws XmlPullParserException, IOException {
    XmlPullParser xpp = new MXParser();
    xpp.setInput(new ByteArrayInputStream(raw.getBytes("UTF-8")), "UTF-8");
    return parseMessage(xpp);
  }

  public SXEMessage parseMessage(XmlPullParser xpp) throws XmlPullParserException, IOException {

    SXEMessage message;
    SXEMessageType messageType = null;
    int eventType = xpp.getEventType();

    if (!xpp.getName().equals(SXEMessage.SXE_TAG)) {
      log.warn("Malformed sxe packet extension. Root tag: " + xpp.getName());
      return null;
    } else {
      message = getMessageInfo(xpp);
      eventType = xpp.next();
    }

    while (eventType != XmlPullParser.END_DOCUMENT) {

      if (eventType == XmlPullParser.START_TAG) {
        if (messageType == null) {
          messageType = SXEMessageType.fromString(xpp.getName());
          if (messageType != null && messageType != SXEMessageType.RECORDS) {
            eventType = xpp.next();
            continue;
          }
          if (messageType == null)
            log.warn("Could not distinguish message type from " + xpp.getName());
        }

        if (isRecord(xpp.getName())) {
          message.setRecords(getRecords(xpp));
          eventType = xpp.getEventType();
        } else if (xpp.getName().equals("description")) {
          // TODO read description
          if (messageType != SXEMessageType.STATE_OFFER)
            log.warn(
                "Wrong entry 'description' in "
                    + messageType
                    + ". Only allowed in "
                    + SXEMessageType.STATE_OFFER
                    + " message.");
        } else if (xpp.getName().equals("document-begin")) {

          if (messageType != SXEMessageType.STATE)
            log.warn(
                "Wrong entry 'document-begin' in "
                    + messageType
                    + ". Only allowed in "
                    + SXEMessageType.STATE
                    + " message.");
        } else if (xpp.getName().equals("document-end")) {

          if (messageType != SXEMessageType.STATE)
            log.warn(
                "Wrong entry 'document-end' in "
                    + messageType
                    + ". Only allowed in "
                    + SXEMessageType.STATE
                    + " message.");
        } else log.warn("Unknown tag: " + xpp.getName() + ". Message type: " + messageType);
      }

      // cannot put to else clause as previous pulls may have incremented
      // the xpp
      if (eventType == XmlPullParser.END_TAG) {
        if (xpp.getName().equals(SXEMessage.SXE_TAG)) break;
      }

      eventType = xpp.next();
    }

    message.setMessageType(messageType);

    return message;
  }
}
