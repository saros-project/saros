package saros.negotiation;

import java.util.List;
import saros.context.IContainerContext;
import saros.editor.IEditorManager;
import saros.filesystem.IChecksumCache;
import saros.filesystem.IWorkspace;
import saros.negotiation.hooks.SessionNegotiationHookManager;
import saros.net.IConnectionManager;
import saros.net.IReceiver;
import saros.net.ITransmitter;
import saros.net.xmpp.JID;
import saros.net.xmpp.XMPPConnectionService;
import saros.net.xmpp.contact.XMPPContactsService;
import saros.observables.FileReplacementInProgressObservable;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.ProjectNegotiationTypeHook;
import saros.session.User;
import saros.versioning.VersionManager;

public final class NegotiationFactory {
  private final VersionManager versionManager;
  private final SessionNegotiationHookManager hookManager;

  // TODO remove, do not use Smack Filetransfer
  private final XMPPConnectionService connectionService;

  /** This is unneeded here if the Factory gets called directly with Contact objects. */
  private final XMPPContactsService contactsService;

  private final IContainerContext context;
  private final FileReplacementInProgressObservable fileReplacementInProgressObservable;
  private final IWorkspace workspace;
  private final IChecksumCache checksumCache;
  private final IConnectionManager connectionManager;
  private final ITransmitter transmitter;
  private final IReceiver receiver;

  private final AdditionalProjectDataFactory additionalProjectDataFactory;

  public NegotiationFactory(
      VersionManager versionManager,
      SessionNegotiationHookManager hookManager,
      XMPPContactsService contactsService,
      FileReplacementInProgressObservable fileReplacementInProgressObservable,
      IWorkspace workspace,
      IChecksumCache checksumCache,
      XMPPConnectionService connectionService,
      IConnectionManager connectionManager,
      ITransmitter transmitter,
      IReceiver receiver,
      AdditionalProjectDataFactory additionalProjectDataFactory,

      /*
       * FIXME HACK for now to avoid cyclic dependencies between this class,
       * the SessionManager and IEditorManager implementations which are using
       * the SessionManager as well.
       */
      IContainerContext context) {

    this.versionManager = versionManager;
    this.hookManager = hookManager;
    this.contactsService = contactsService;
    this.context = context;
    this.fileReplacementInProgressObservable = fileReplacementInProgressObservable;
    this.workspace = workspace;
    this.checksumCache = checksumCache;
    this.connectionService = connectionService;
    this.connectionManager = connectionManager;
    this.transmitter = transmitter;
    this.receiver = receiver;
    this.additionalProjectDataFactory = additionalProjectDataFactory;
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

  public AbstractOutgoingProjectNegotiation newOutgoingProjectNegotiation(
      JID remoteAddress,
      ProjectSharingData projectSharingData,
      ISarosSessionManager sessionManager,
      ISarosSession session) {

    switch (getTransferType(session, remoteAddress)) {
      case ARCHIVE:
        return new ArchiveOutgoingProjectNegotiation(
            remoteAddress,
            projectSharingData,
            sessionManager,
            session,
            context.getComponent(IEditorManager.class),
            workspace,
            checksumCache,
            connectionService,
            transmitter,
            receiver,
            additionalProjectDataFactory);
      case INSTANT:
        return new InstantOutgoingProjectNegotiation(
            remoteAddress,
            projectSharingData,
            sessionManager,
            session,
            context.getComponent(IEditorManager.class),
            workspace,
            checksumCache,
            connectionService,
            transmitter,
            receiver,
            additionalProjectDataFactory);
      default:
        throw new UnsupportedOperationException("transferType not implemented");
    }
  }

  public AbstractIncomingProjectNegotiation newIncomingProjectNegotiation(
      JID remoteAddress,
      String negotiationID,
      List<ProjectNegotiationData> projectNegotiationData,
      ISarosSessionManager sessionManager,
      ISarosSession session) {

    switch (getTransferType(session, remoteAddress)) {
      case ARCHIVE:
        return new ArchiveIncomingProjectNegotiation(
            remoteAddress,
            negotiationID,
            projectNegotiationData,
            sessionManager,
            session,
            fileReplacementInProgressObservable,
            workspace,
            checksumCache,
            connectionService,
            transmitter,
            receiver);
      case INSTANT:
        return new InstantIncomingProjectNegotiation(
            remoteAddress,
            negotiationID,
            projectNegotiationData,
            sessionManager,
            session,
            fileReplacementInProgressObservable,
            workspace,
            checksumCache,
            connectionService,
            transmitter,
            receiver);
      default:
        throw new UnsupportedOperationException("transferType not implemented");
    }
  }

  private TransferType getTransferType(ISarosSession session, JID remoteAddress) {
    User user = session.getUser(remoteAddress);
    if (user == null) {
      throw new IllegalStateException("User <" + user + "> is not part of the session.");
    }

    String type = user.getPreferences().getString(ProjectNegotiationTypeHook.KEY_TYPE);
    if (type.isEmpty()) {
      throw new IllegalArgumentException("Missing TransferType for User: " + user);
    }

    return TransferType.valueOf(type);
  }
}
