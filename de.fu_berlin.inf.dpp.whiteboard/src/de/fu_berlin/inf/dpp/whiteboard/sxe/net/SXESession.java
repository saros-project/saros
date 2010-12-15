package de.fu_berlin.inf.dpp.whiteboard.sxe.net;

import java.util.List;
import java.util.Random;

import de.fu_berlin.inf.dpp.whiteboard.sxe.constants.SXEMessageType;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.serializable.RecordDataObject;

/**
 * Simple session object to store IDs and manage a message counter
 * 
 * @author jurke
 * 
 */
public class SXESession {

	private static final Random RANDOM = new Random();

	protected String sessionId;

	private int msgCount = 0;

	public SXESession(String sessionId) {
		this.sessionId = sessionId;
	}

	public SXESession() {
		this(String.valueOf(RANDOM.nextInt(10000)));
	}

	protected String getNextMessageId() {
		return String.valueOf(msgCount++);// +user.getBase();
	}

	public SXEMessage getNextMessage(SXEMessageType type, String to) {
		if (sessionId == null)
			throw new RuntimeException("SXE Sender not initialized");
		SXEMessage msg = new SXEMessage(this, getNextMessageId());
		msg.setMessageType(type);
		msg.setTo(to);
		return msg;
	}

	public SXEMessage getNextMessage(SXEMessageType type) {
		return getNextMessage(type, null);
	}

	public String prefix() {
		return "SXE(sessionId) ";
	}

	public String getSessionId() {
		return sessionId;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof SXESession)
			return equals((SXESession) o);
		return false;
	}

	public boolean equals(SXESession session) {
		return sessionId.equals(session.getSessionId());
	}

	@Override
	public String toString() {
		return sessionId;
	}

	/**
	 * At the moment this message object is used for all SXE messages. The tags
	 * are defined by the SXEMessageType.
	 * 
	 * @author jurke
	 * 
	 */
	/*
	 * Note: to be extended/subclassed if completely implementing SXE.
	 * Description and prolog are not contained yet.
	 */
	public static class SXEMessage {

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

	}

}
