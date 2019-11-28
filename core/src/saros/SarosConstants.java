package saros;

public interface SarosConstants {

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
