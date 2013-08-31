package de.fu_berlin.inf.dpp.net.internal;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.fu_berlin.inf.dpp.net.JID;

public class TransferDescriptionTest {

    @Test
    public void testByteArray() throws Exception {

        TransferDescription td = TransferDescription
            .createCustomTransferDescription();

        td.setType("foo");
        td.setNamespace("bar");
        td.setSessionID("4711");
        td.setRecipient(new JID("alice@test"));
        td.setSender(new JID("bob@test"));
        td.setArchivePath("foo/bar");
        td.setSize(0xFFFFF);
        td.setCompressContent(true);

        byte[] data = TransferDescription.toByteArray(td);
        TransferDescription td2 = TransferDescription.fromByteArray(data);

        assertEquals(td.getType(), td2.getType());
        assertEquals(td.getNamespace(), td2.getNamespace());
        assertEquals(td.getSessionID(), td2.getSessionID());
        assertEquals(td.getRecipient(), td2.getRecipient());
        assertEquals(td.getSender(), td2.getSender());
        assertEquals(td.getArchivePath(), td2.getArchivePath());
        assertEquals(td.getSize(), td2.getSize());
        assertEquals(td.compressContent(), td2.compressContent());
    }
}
