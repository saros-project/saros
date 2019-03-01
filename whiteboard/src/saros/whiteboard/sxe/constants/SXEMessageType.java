package saros.whiteboard.sxe.constants;

/**
 * Enumeration for SXE messages used during session negotiation.
 *
 * <p>Each constant but {@link #RECORDS} is enhanced by its XML tag to be used when serialized.
 * Accessible by the toString()-method.</br>
 *
 * <p>The static utility method fromString(String) facilitates deserialization. It also checks
 * whether the provided tag is a {@link RecordType} and will return {@link #RECORDS} respectively.
 *
 * @author jurke
 */
public enum SXEMessageType {

  // Note, RECORDS do not have a corresponding tag name
  RECORDS("records"),
  STATE_OFFER("state-offer"),
  REFUSE_STATE("refuse-state"),
  ACCEPT_STATE("accept-state"),
  ACK_STATE("ack-state"),
  STATE("state");

  private String name;

  private SXEMessageType(String name, boolean isInitialStateSynchronization) {
    this.name = name;
  }

  private SXEMessageType(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }

  public static SXEMessageType fromString(String raw) {
    if (raw.equals(RECORDS.toString())) return null;
    for (SXEMessageType t : SXEMessageType.values()) if (t.toString().equals(raw)) return t;
    if (RecordType.fromString(raw) != null) return RECORDS;
    return null;
  }
}
