package de.fu_berlin.inf.dpp;

public interface SarosConstants {

    /**
     * This is the Bundle-SymbolicName (a.k.a the pluginID)
     */
    public static final String SAROS = "de.fu_berlin.inf.dpp"; //$NON-NLS-1$
    /**
     * The name of the XMPP namespace used by Saros. At the moment it is only
     * used to advertise the Saros feature in the Service Discovery.
     * 
     * TODO Add version information, so that only compatible versions of Saros
     * can use each other.
     */
    public final static String NAMESPACE = SAROS;
    /**
     * Sub-namespace for the server. It is used advertise when a server is
     * active.
     */
    public static final String NAMESPACE_SERVER = NAMESPACE + ".server"; //$NON-NLS-1$
    /**
     * The name of the resource identifier used by Saros when connecting to the
     * XMPP server (for instance when logging in as john@doe.com, Saros will
     * connect using john@doe.com/Saros)
     * 
     * @deprecated Do not use this resource identifier to build a fully
     *             qualified Jabber identifier, e.g the logic connects to a XMPP
     *             server as foo@bar/Saros but the assigned Jabber identifier
     *             may be something like foo@bar/Saros765E18ED !
     */
    @Deprecated
    public final static String RESOURCE = "Saros"; //$NON-NLS-1$

}
