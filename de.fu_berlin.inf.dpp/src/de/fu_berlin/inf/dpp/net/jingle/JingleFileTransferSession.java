package de.fu_berlin.inf.dpp.net.jingle;

import org.jivesoftware.smackx.jingle.JingleSession;
import org.jivesoftware.smackx.jingle.media.JingleMediaSession;
import org.jivesoftware.smackx.jingle.media.PayloadType;
import org.jivesoftware.smackx.jingle.nat.TransportCandidate;

public class JingleFileTransferSession extends JingleMediaSession {

    public JingleFileTransferSession(PayloadType payloadType,
	    TransportCandidate remote, TransportCandidate local,
	    String mediaLocator, JingleSession jingleSession) {
	super(payloadType, remote, local, mediaLocator, jingleSession);
	// TODO Auto-generated constructor stub
    }

    @Override
    public void initialize() {
	// TODO Auto-generated method stub

    }

    @Override
    public void setTrasmit(boolean active) {
	// TODO Auto-generated method stub

    }

    @Override
    public void startReceive() {
	// TODO Auto-generated method stub

    }

    @Override
    public void startTrasmit() {
	// TODO Auto-generated method stub

    }

    @Override
    public void stopReceive() {
	// TODO Auto-generated method stub

    }

    @Override
    public void stopTrasmit() {
	// TODO Auto-generated method stub

    }

}
