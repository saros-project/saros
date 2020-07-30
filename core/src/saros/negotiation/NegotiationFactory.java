package saros.negotiation;

import saros.negotiation.hooks.SessionNegotiationHookManager;
import saros.net.IConnectionManager;
import saros.net.IReceiver;
import saros.net.ITransmitter;
import saros.net.xmpp.JID;
import saros.net.xmpp.contact.XMPPContactsService;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.versioning.VersionManager;

public final class NegotiationFactory {
  private final VersionManager versionManager;
  private final SessionNegotiationHookManager hookManager;

  /** This is unneeded here if the Factory gets called directly with Contact objects. */
  private final XMPPContactsService contactsService;

  private final IConnectionManager connectionManager;
  private final ITransmitter transmitter;
  private final IReceiver receiver;

  public NegotiationFactory(
      VersionManager versionManager,
      SessionNegotiationHookManager hookManager,
      XMPPContactsService contactsService,
      IConnectionManager connectionManager,
      ITransmitter transmitter,
      IReceiver receiver) {

    this.versionManager = versionManager;
    this.hookManager = hookManager;
    this.contactsService = contactsService;
    this.connectionManager = connectionManager;
    this.transmitter = transmitter;
    this.receiver = receiver;
  }

  public OutgoingSessionNegotiation newOutgoingSessionNegotiation(
      JID remoteAddress,
      ISarosSessionManager sessionManager,
      ISarosSession session,
      String description) {

    return new OutgoingSessionNegotiation(
        remoteAddress,
        description,
        sessionManager,
        session,
        hookManager,
        versionManager,
        contactsService,
        transmitter,
        receiver);
  }

  public IncomingSessionNegotiation newIncomingSessionNegotiation(
      final JID remoteAddress,
      final String negotiationID,
      final String sessionID,
      final String remoteVersion,
      final ISarosSessionManager sessionManager,
      final String description) {

    return new IncomingSessionNegotiation(
        remoteAddress,
        negotiationID,
        sessionID,
        remoteVersion,
        description,
        sessionManager,
        hookManager,
        connectionManager,
        transmitter,
        receiver);
  }
}
