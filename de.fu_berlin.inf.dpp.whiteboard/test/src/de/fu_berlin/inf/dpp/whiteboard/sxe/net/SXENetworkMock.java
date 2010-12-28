package de.fu_berlin.inf.dpp.whiteboard.sxe.net;

public class SXENetworkMock {

	public ISXETransmitter getClientMock() {
		return new MockedSXETransmitter(this);
	}

	// public ISXETransmitter getHostMock();

}
