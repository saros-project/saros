package de.fu_berlin.inf.dpp.whiteboard.sxe.net;

import de.fu_berlin.inf.dpp.whiteboard.sxe.constants.SXEMessageType;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.serializable.RecordDataObject;
import java.util.List;

/**
 * At the moment this message object is used for all SXE messages. The tags are defined by the
 * SXEMessageType.
 *
 * @author jurke
 */
/*
 * Note: to be extended/subclassed if completely implementing SXE. Description
 * and prolog are not contained yet.
 */
public class SXEMessage {

  public static final String SXE_TAG = "sxe";
  public static final String SXE_XMLNS = "urn:xmpp:sxe:0";

  private final SXESession session;
  private final String msgId;

  private SXEMessageType messageType;
  private List<RecordDataObject> records;

  private String from;
  private String to;

  // private String sessionName;

  public SXEMessage(SXESession sessionId, String msgId) {
    this.session = sessionId;
    this.msgId = msgId;
  }

  public SXESession getSession() {
    return session;
  }

  public String getMessageId() {
    return msgId;
  }

  public SXEMessageType getMessageType() {
    return messageType;
  }

  public void setMessageType(SXEMessageType messageType) {
    this.messageType = messageType;
  }

  public String getFrom() {
    return from;
  }

  public void setFrom(String from) {
    this.from = from;
  }

  public String getTo() {
    return to;
  }

  public void setTo(String to) {
    this.to = to;
  }

  public List<RecordDataObject> getRecords() {
    return records;
  }

  public void setRecords(List<RecordDataObject> records) {
    this.records = records;
  }

  @Override
  public String toString() {
    String out = "";
    out += "from: " + from;
    out += ", to: " + to;
    out += ", msgId: " + msgId;
    out += "messageTyp: " + messageType;
    out += "message: [";
    for (RecordDataObject rec : records) {
      out += rec + ", ";
    }
    out += "]";

    return super.toString() + ": " + out;
  }
}
