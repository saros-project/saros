package saros.negotiation;

import java.util.List;
import saros.editor.IEditorManager;
import saros.filesystem.IWorkspace;
import saros.filesystem.checksum.IChecksumCache;
import saros.net.IReceiver;
import saros.net.ITransmitter;
import saros.net.xmpp.JID;
import saros.net.xmpp.filetransfer.XMPPFileTransferManager;
import saros.observables.FileReplacementInProgressObservable;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.ResourceNegotiationTypeHook;
import saros.session.User;

/** Factory to create resource negotiation objects. */
public class ResourceNegotiationFactory {
  private final XMPPFileTransferManager fileTransferManager;
  private final IEditorManager editorManager;
  private final FileReplacementInProgressObservable fileReplacementInProgressObservable;
  private final IWorkspace workspace;
  private final IChecksumCache checksumCache;
  private final ITransmitter transmitter;
  private final IReceiver receiver;

  private final AdditionalResourceDataFactory additionalResourceDataFactory;

  public ResourceNegotiationFactory(
      XMPPFileTransferManager fileTransferManager,
      IEditorManager editorManager,
      FileReplacementInProgressObservable fileReplacementInProgressObservable,
      IWorkspace workspace,
      IChecksumCache checksumCache,
      ITransmitter transmitter,
      IReceiver receiver,
      AdditionalResourceDataFactory additionalResourceDataFactory) {

    this.fileTransferManager = fileTransferManager;
    this.editorManager = editorManager;
    this.fileReplacementInProgressObservable = fileReplacementInProgressObservable;
    this.workspace = workspace;

    this.checksumCache = checksumCache;
    this.transmitter = transmitter;
    this.receiver = receiver;
    this.additionalResourceDataFactory = additionalResourceDataFactory;
  }

  public AbstractOutgoingResourceNegotiation newOutgoingResourceNegotiation(
      JID remoteAddress,
      ResourceSharingData resourceSharingData,
      ISarosSessionManager sessionManager,
      ISarosSession session) {

    switch (getTransferType(session, remoteAddress)) {
      case ARCHIVE:
        return new ArchiveOutgoingResourceNegotiation(
            remoteAddress,
            resourceSharingData,
            sessionManager,
            session,
            editorManager,
            workspace,
            checksumCache,
            fileTransferManager,
            transmitter,
            receiver,
            additionalResourceDataFactory);
      case INSTANT:
        return new InstantOutgoingResourceNegotiation(
            remoteAddress,
            resourceSharingData,
            sessionManager,
            session,
            editorManager,
            workspace,
            checksumCache,
            fileTransferManager,
            transmitter,
            receiver,
            additionalResourceDataFactory);
      default:
        throw new UnsupportedOperationException("transferType not implemented");
    }
  }

  public AbstractIncomingResourceNegotiation newIncomingResourceNegotiation(
      JID remoteAddress,
      String negotiationID,
      List<ResourceNegotiationData> resourceNegotiationData,
      ISarosSessionManager sessionManager,
      ISarosSession session) {

    switch (getTransferType(session, remoteAddress)) {
      case ARCHIVE:
        return new ArchiveIncomingResourceNegotiation(
            remoteAddress,
            negotiationID,
            resourceNegotiationData,
            sessionManager,
            session,
            fileReplacementInProgressObservable,
            workspace,
            checksumCache,
            fileTransferManager,
            transmitter,
            receiver);
      case INSTANT:
        return new InstantIncomingResourceNegotiation(
            remoteAddress,
            negotiationID,
            resourceNegotiationData,
            sessionManager,
            session,
            fileReplacementInProgressObservable,
            workspace,
            checksumCache,
            fileTransferManager,
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

    String type = user.getPreferences().getString(ResourceNegotiationTypeHook.KEY_TYPE);
    if (type.isEmpty()) {
      throw new IllegalArgumentException("Missing TransferType for User: " + user);
    }

    return TransferType.valueOf(type);
  }
}
