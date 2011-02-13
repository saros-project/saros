package de.fu_berlin.inf.dpp.net.internal;

import java.io.IOException;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.net.IncomingTransferObject;
import de.fu_berlin.inf.dpp.net.IncomingTransferObject.IncomingTransferObjectExtensionProvider;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.NetTransferMode;
import de.fu_berlin.inf.dpp.net.internal.TransferDescription.FileTransferType;
import de.fu_berlin.inf.dpp.net.internal.discoveryManager.DiscoveryManager;
import de.fu_berlin.inf.dpp.util.StopWatch;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * Class responsible for testing the data transfer connection using the data
 * transfer manager.
 * 
 * @author sszuecs
 * @author coezbek
 */
public class ConnectionTestManager {

    private static final Logger log = Logger
        .getLogger(ConnectionTestManager.class);

    /**
     * Data returned from the user who received a {@link TransferDescription}
     * with type {@value FileTransferType#CONNECTION_TEST}
     */
    public static class ConnectionTestResponse {

        public long dataHash;

        public String errorMessage;

        public NetTransferMode transferMode;
    }

    protected XStreamExtensionProvider<ConnectionTestResponse> responseProvider = new XStreamExtensionProvider<ConnectionTestResponse>(
        "sarosConnectionTestResult", ConnectionTestResponse.class);

    @Inject
    protected Saros saros;

    @Inject
    protected DataTransferManager dataTransferManager;

    @Inject
    protected XMPPTransmitter transmitter;

    @Inject
    protected DiscoveryManager discoManager;

    public ConnectionTestManager(
        XMPPReceiver receiver,
        final IncomingTransferObjectExtensionProvider incomingTransferObjectExtensionProvider) {

        receiver.addPacketListener(new PacketListener() {

            public void processPacket(Packet packet) {

                IncomingTransferObject ito = incomingTransferObjectExtensionProvider
                    .getPayload(packet);

                ConnectionTestResponse result = new ConnectionTestResponse();
                result.transferMode = ito.getTransferMode();

                try {
                    byte[] data = ito.accept(SubMonitor
                        .convert(new NullProgressMonitor()));

                    result.dataHash = Arrays.hashCode(data);

                    log.info(Utils.prefix(new JID(packet.getFrom()))
                        + "Connection Test Data received: " + data.length
                        + " bytes, hashCode==" + result.dataHash);
                } catch (SarosCancellationException e) {
                    log.error(
                        "Connection Test failed because of an CancelationException",
                        e);
                    result.errorMessage = "SarosCancellationException: "
                        + Utils.getMessage(e);
                } catch (IOException e) {
                    log.error(
                        "Connection Test failed because of an IOException", e);
                    result.errorMessage = "IOException: " + Utils.getMessage(e);
                }

                try {
                    IQ iqResponse = responseProvider.createIQ(result);
                    iqResponse.setTo(packet.getFrom());
                    iqResponse.setPacketID(ito.getTransferDescription().testID);

                    saros.getConnection().sendPacket(iqResponse);
                } catch (Exception e) {
                    log.error(
                        "Could not send test results to "
                            + Utils.prefix(new JID(packet.getFrom())), e);
                }
            }
        }, new PacketFilter() {

            public boolean accept(Packet packet) {
                IncomingTransferObject payload = incomingTransferObjectExtensionProvider
                    .getPayload(packet);

                if (payload == null)
                    return false;

                return FileTransferType.CONNECTION_TEST.equals(payload
                    .getTransferDescription().type);
            }
        });

    }

    /**
     * Create a byte[] filled with random data
     */
    public static byte[] getTestArray(int size) {

        byte[] result = new byte[size];
        Saros.RANDOM.nextBytes(result);
        return result;
    }

    /**
     * Result of a ConnectionTest
     */
    public static class TestResult {

        public NetTransferMode mode;

        public long transferTime;

        public int dataSize;

    }

    /**
     * Run a connection test with the given user by transmitting a random byte[]
     * of the given size using the {@link DataTransferManager}.
     * 
     * Progress will be reported via the given monitor, errors via
     * XMPPExceptions and results of a successful test via the returned
     * TestResult
     */
    public TestResult runConnectionTest(JID plainJID, int size,
        SubMonitor progress) throws XMPPException {

        TestResult result = new TestResult();
        result.dataSize = size;

        progress.beginTask("Connection Test with buddy " + plainJID, 68);
        try {
            XMPPConnection connection = saros.getConnection();
            if (connection == null || !connection.isConnected())
                throw new XMPPException("Connection is not established!");
            progress.worked(1);

            String id = Packet.nextID();

            progress.subTask("Checking if buddy is using Saros");
            JID user = discoManager.getSupportingPresence(plainJID,
                Saros.NAMESPACE);
            if (user == null)
                throw new XMPPException("Buddy " + plainJID
                    + " is not using Saros");
            progress.worked(1);

            TransferDescription transferData = TransferDescription
                .createTestTransferDescription(user, id, saros.getMyJID());

            progress.subTask("Generating Test Data");
            byte[] testData = getTestArray(size);
            progress.worked(1);

            // Create a packet collector to listen for a response.
            PacketCollector collector = connection
                .createPacketCollector(new PacketIDFilter(id));

            StopWatch watch = new StopWatch().start();

            try {
                try {
                    progress.subTask("Sending Data");
                    dataTransferManager.sendData(transferData, testData,
                        progress.newChild(40));
                } catch (IOException e) {
                    throw new XMPPException("IOException sending data", e);
                } catch (SarosCancellationException e) {
                    throw new XMPPException(
                        "CancellationException sending data", e);
                }

                progress.subTask("Waiting for reply");
                ConnectionTestResponse response = null;
                for (int i = 0; i < 15; i++) {
                    response = responseProvider.getPayload(collector
                        .nextResult(1000));
                    if (response != null)
                        break;
                    progress.worked(1);
                }

                result.transferTime = watch.stop().getTime();

                if (response == null)
                    throw new XMPPException("Timeout after 15s");

                if (response.errorMessage != null)
                    throw new XMPPException("An remote error occurred: "
                        + response.errorMessage);

                int localDataHash = Arrays.hashCode(testData);
                if (response.dataHash != localDataHash)
                    throw new XMPPException(
                        "Hash results don't match: Received=="
                            + response.dataHash + " expected==" + localDataHash);

                result.mode = response.transferMode;

            } finally {
                collector.cancel();
            }

        } finally {
            progress.done();
        }
        return result;
    }
}
