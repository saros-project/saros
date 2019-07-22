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
import saros.net.xmpp.discovery.DiscoveryManager;
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
  /*
   * TODO is this really needed ? The only usage is to obtain the RQJID via
   * the discoveryManager which is already not a good practice, also there is
   * no need to check the support here as the negotiation will fail if the
   * remote side does not support Saros. Checking the support should be done
   * before the negotiation is started, and if we want to check the support it
   * should be made using our own protocol and not some handy and dandy XMPP
   * stuff which can be faked anyway, the request may timeout, etc.
   */
  private final DiscoveryManager discoveryManager;

  // private final IEditorManager editorManager;
  private final IContainerContext context;

  private final FileReplacementInProgressObservable fileReplacementInProgressObservable;

  private final IWorkspace workspace;
  private final IChecksumCache checksumCache;

  private final IConnectionManager connectionManager;
  private final ITransmitter transmitter;
  private final IReceiver receiver;

  public NegotiationFactory(
      final VersionManager versionManager, //
      final SessionNegotiationHookManager hookManager, //
      final DiscoveryManager discoveryManager, //
      // final IEditorManager editorManager, //
      final FileReplacementInProgressObservable fileReplacementInProgressObservable, //
      final IWorkspace workspace, //
      final IChecksumCache checksumCache, //
      final XMPPConnectionService connectionService, //
      final IConnectionManager connectionManager, //
      final ITransmitter transmitter, //
      final IReceiver receiver, //

      /*
       * FIXME HACK for now to avoid cyclic dependencies between this class,
       * the SessionManager and IEditorManager implementations which are using
       * the SessionManager as well.
       */
      final IContainerContext context //
      ) {

    this.versionManager = versionManager;
    this.hookManager = hookManager;
    this.discoveryManager = discoveryManager;

    // this.editorManager = editorManager;
    this.context = context;

    this.fileReplacementInProgressObservable = fileReplacementInProgressObservable;

    this.workspace = workspace;
    this.checksumCache = checksumCache;

    this.connectionService = connectionService;

    this.connectionManager = connectionManager;
    this.transmitter = transmitter;
    this.receiver = receiver;
  }

  public OutgoingSessionNegotiation newOutgoingSessionNegotiation(
      final JID remoteAddress,
      final ISarosSessionManager sessionManager,
      final ISarosSession session,
      final String description) {

    return new OutgoingSessionNegotiation(
        remoteAddress,
        description,
        sessionManager,
        session,
        hookManager,
        versionManager,
        discoveryManager,
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
      final JID remoteAddress,
      final ProjectSharingData projectSharingData,
      final ISarosSessionManager sessionManager,
      final ISarosSession session) {

    TransferType transferType = getTransferType(session, remoteAddress);

    switch (transferType) {
      case ARCHIVE:
        return new ArchiveOutgoingProjectNegotiation(
            remoteAddress,
            projectSharingData,
            sessionManager,
            session, /* editorManager */
            context.getComponent(IEditorManager.class),
            workspace,
            checksumCache,
            connectionService,
            transmitter,
            receiver);
      case INSTANT:
        return new InstantOutgoingProjectNegotiation(
            remoteAddress,
            projectSharingData,
            sessionManager,
            session, /* editorManager */
            context.getComponent(IEditorManager.class),
            workspace,
            checksumCache,
            connectionService,
            transmitter,
            receiver);
      default:
        throw new UnsupportedOperationException("transferType not implemented");
    }
  }

  public AbstractIncomingProjectNegotiation newIncomingProjectNegotiation(
      final JID remoteAddress,
      final String negotiationID,
      final List<ProjectNegotiationData> projectNegotiationData,
      final ISarosSessionManager sessionManager,
      final ISarosSession session) {

    TransferType transferType = getTransferType(session, remoteAddress);

    switch (transferType) {
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
