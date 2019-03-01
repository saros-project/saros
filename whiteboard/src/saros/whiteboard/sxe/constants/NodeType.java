package saros.whiteboard.sxe.constants;

/**
 * Enumeration for the type attribute value of a new record respective SXE.
 *
 * <p>Each constant is enhanced by its XML tag to be used when serialized. Accessible by the
 * toString()-method.</br>
 *
 * <p>The static utility method fromString(String) facilitates deserialization.
 *
 * @author jurke
 */
public enum NodeType {
  ELEMENT("element"),
  ATTR("attr")
// , TEXT("text"),
// COMMENT("comment"),PROCESSINGINSTRUCTION("processinginstruction")
;

  private String type;

  private NodeType(String type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return type;
  }

  public static NodeType fromString(String raw) {
    for (NodeType t : NodeType.values()) if (t.toString().equals(raw)) return t;
    return null;
  }
}
