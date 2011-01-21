package de.fu_berlin.inf.dpp.whiteboard.sxe.net;

import java.io.IOException;

import org.eclipse.core.runtime.SubMonitor;

import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.whiteboard.sxe.SXEController;
import de.fu_berlin.inf.dpp.whiteboard.sxe.constants.SXEMessageType;

public class MockedSXETransmitter implements ISXETransmitter {

	private SXENetworkMock network;

	public MockedSXETransmitter(SXENetworkMock network) {

	}

	@Override
	public void sendAsync(SXEMessage msg) {

	}

	@Override
	public SXEMessage sendAndAwait(SubMonitor monitor, SXEMessage msg,
			SXEMessageType... awaitFor) throws IOException,
			LocalCancellationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void installRecordReceiver(SXEController controller) {
		// TODO Auto-generated method stub

	}

}
