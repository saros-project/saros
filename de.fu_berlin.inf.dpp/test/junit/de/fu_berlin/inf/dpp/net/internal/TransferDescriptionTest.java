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
        assertEquals(td.getSessionID(), td2.getSessionID());
        assertEquals(td.getProcessID(), td2.getProcessID());
        assertEquals(td.getSender(), td2.getSender());
        assertEquals(td.getRecipient(), td2.getRecipient());
    }
}
