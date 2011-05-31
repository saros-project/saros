package de.fu_berlin.inf.dpp.net.internal;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import de.fu_berlin.inf.dpp.net.JID;

public class TransferDescriptionTest {

    private TransferDescription td;
    private JID recipient;
    private JID sender;
    private String processID;
    private String sessionID;

    @Before
    public void setUp() throws Exception {
        recipient = new JID("receiver@foo");
        sender = new JID("sender@bar");
        processID = "Process1";
        sessionID = "session";
        td = TransferDescription.createFileListTransferDescription(recipient,
            sender, sessionID, processID);
    }

    @Test
    public void testByteArray() throws ClassNotFoundException {
        byte[] data = td.toByteArray();
        TransferDescription td2 = TransferDescription.fromByteArray(data);
        assertEquals(td.sessionID, td2.sessionID);
        assertEquals(td.processID, td2.processID);
        assertEquals(td.sender, td2.sender);
        assertEquals(td.recipient, td2.recipient);
    }
}
