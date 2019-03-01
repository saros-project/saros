package de.fu_berlin.inf.dpp.communication.extensions;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("SSRs")
public class SessionStatusResponseExtension extends SarosPacketExtension {

  public static final Provider PROVIDER = new Provider();

  private boolean isInSession;
  private int participants;
  private String sessionDescription;

  public SessionStatusResponseExtension() {
    isInSession = false;
    participants = -1;
  }

  /**
   * Constructor for SessionStatusResponseExtension.
   *
   * @param participants number of participants, server not counting
   * @param sessionDescription a string that describes the session, is shown to the receiver
   */
  public SessionStatusResponseExtension(int participants, String sessionDescription) {

    if (participants < 0)
      throw new IllegalArgumentException("Invalid number of participants: " + participants);
    if (sessionDescription == null)
      throw new IllegalArgumentException("Session description is null");

    isInSession = true;
    this.participants = participants;
    this.sessionDescription = sessionDescription;
  }

  public boolean isInSession() {
    return isInSession;
  }

  /** @return a session description or <code>null</code> if no session is running */
  public String getSessionDescription() {
    return sessionDescription;
  }

  /** @return number of participants, server not counting or -1 if no session is running */
  public int getNumberOfParticipants() {
    return participants;
  }

  public static class Provider
      extends SarosPacketExtension.Provider<SessionStatusResponseExtension> {
    private Provider() {
      super("sessionStatusResponse", SessionStatusResponseExtension.class);
    }
  }
}
