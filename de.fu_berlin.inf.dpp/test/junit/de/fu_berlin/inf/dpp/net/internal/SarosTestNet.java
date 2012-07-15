package de.fu_berlin.inf.dpp.net.internal;

import de.fu_berlin.inf.dpp.net.IncomingTransferObject.IncomingTransferObjectExtensionProvider;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.RosterTracker;
import de.fu_berlin.inf.dpp.net.SarosNet;
import de.fu_berlin.inf.dpp.net.business.DispatchThreadContext;
import de.fu_berlin.inf.dpp.net.internal.DefaultInvitationInfo.UserListRequestExtensionProvider;
import de.fu_berlin.inf.dpp.net.internal.InvitationInfo.InvitationExtensionProvider;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;

/**
 * Encapsulates a network instance of Saros, including all relevant network
 * objects to communicate between two instances. Uses setter dependency
 * injections.
 */
public class SarosTestNet {

    // the network
    public JID jid;
    public SarosNet net;
    public SessionIDObservable sessionID;
    public IBBTransport ibb;
    public Socks5Transport socks5Transport;
    public DataTransferManager dtm;
    public XMPPReceiver xmppReceiver;
    public XMPPTransmitter xmppTransmitter;
    public RosterTracker rosterTracker;

    public SarosTestNet(String user, String server) {

        jid = new JID(user + "@" + server);

        net = new SarosNet(null, null);
        net.setSettings(false, true, 0, "", 0, false);
        net.initialize();

        ibb = new IBBTransport();
        socks5Transport = new Socks5Transport();

        DispatchThreadContext dispatchThreadContext = new DispatchThreadContext();
        IncomingTransferObjectExtensionProvider incomingTransferObjectExtensionProvider = new IncomingTransferObjectExtensionProvider();

        rosterTracker = new RosterTracker(net);

        dtm = new DataTransferManager(net, null, rosterTracker, ibb,
            socks5Transport);
        xmppReceiver = new XMPPReceiver();
        dtm.inject(xmppReceiver, dispatchThreadContext,
            incomingTransferObjectExtensionProvider);

        xmppReceiver.inject(incomingTransferObjectExtensionProvider,
            dispatchThreadContext);

        xmppTransmitter = new XMPPTransmitter(sessionID, dtm, net,
            xmppReceiver, new UserListRequestExtensionProvider(),
            new InvitationExtensionProvider());

    }
}