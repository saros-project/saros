package de.fu_berlin.inf.dpp.negotiation;

import de.fu_berlin.inf.dpp.context.IContainerContext;
import de.fu_berlin.inf.dpp.editor.IEditorManager;
import de.fu_berlin.inf.dpp.filesystem.IChecksumCache;
import de.fu_berlin.inf.dpp.filesystem.IPathFactory;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import de.fu_berlin.inf.dpp.filesystem.IWorkspace;
import de.fu_berlin.inf.dpp.negotiation.hooks.SessionNegotiationHookManager;
import de.fu_berlin.inf.dpp.net.IConnectionManager;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.net.xmpp.discovery.DiscoveryManager;
import de.fu_berlin.inf.dpp.observables.FileReplacementInProgressObservable;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.versioning.VersionManager;
import java.util.List;

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
  private final IPathFactory pathFactory;

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
      final IContainerContext context, //
      final IPathFactory pathFactory) {

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
    this.pathFactory = pathFactory;
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
      final TransferType transferType,
      final List<IReferencePoint> referencePoints,
      final ISarosSessionManager sessionManager,
      final ISarosSession session) {

    if (transferType == null) {
      throw new IllegalArgumentException("transferType must not be null");
    }

    switch (transferType) {
      case ARCHIVE:
        return new ArchiveOutgoingProjectNegotiation(
            remoteAddress,
            referencePoints,
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
            referencePoints,
            sessionManager,
            session, /* editorManager */
            context.getComponent(IEditorManager.class),
            workspace,
            checksumCache,
            connectionService,
            transmitter,
            receiver,
            pathFactory);
      default:
        throw new UnsupportedOperationException("transferType not implemented");
    }
  }

  public AbstractIncomingProjectNegotiation newIncomingProjectNegotiation(
      final JID remoteAddress,
      final TransferType transferType,
      final String negotiationID,
      final List<ProjectNegotiationData> projectNegotiationData,
      final ISarosSessionManager sessionManager,
      final ISarosSession session) {

    if (transferType == null) {
      throw new IllegalArgumentException("transferType must not be null");
    }

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
}
