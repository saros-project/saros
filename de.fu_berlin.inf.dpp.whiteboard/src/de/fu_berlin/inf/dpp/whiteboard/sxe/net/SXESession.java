package de.fu_berlin.inf.dpp.whiteboard.sxe.net;

import de.fu_berlin.inf.dpp.whiteboard.sxe.constants.SXEMessageType;
import java.util.Random;

/**
 * Simple session object to store IDs and manage a message counter
 *
 * @author jurke
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
    return String.valueOf(msgCount++); // +user.getBase();
  }

  public SXEMessage getNextMessage(SXEMessageType type, String to) {
    if (sessionId == null) throw new RuntimeException("SXE Sender not initialized");
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
    if (o instanceof SXESession) return equals((SXESession) o);
    return false;
  }

  public boolean equals(SXESession session) {
    return sessionId.equals(session.getSessionId());
  }

  @Override
  public String toString() {
    return sessionId;
  }
}
