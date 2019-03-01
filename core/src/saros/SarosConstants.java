package saros;

public interface SarosConstants {

  /**
   * XMPP namespace of the Saros Feature.
   *
   * <p>See also: XEP-0030: Service Discovery (http://xmpp.org/extensions/xep-0030.html)
   */
  public static final String XMPP_FEATURE_NAMESPACE = "saros";
  /** Sub-namespace for the server. It is used advertise when a server is active. */
  public static final String NAMESPACE_SERVER = XMPP_FEATURE_NAMESPACE + ".server";
  /**
   * The name of the resource identifier used by Saros when connecting to the XMPP server (for
   * instance when logging in as john@doe.com, Saros will connect using john@doe.com/Saros)
   *
   * @deprecated Do not use this resource identifier to build a fully qualified Jabber identifier,
   *     e.g the logic connects to a XMPP server as foo@bar/Saros but the assigned Jabber identifier
   *     may be something like foo@bar/Saros765E18ED !
   */
  @Deprecated public static final String RESOURCE = "Saros";
}
