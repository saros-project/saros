package de.fu_berlin.inf.dpp.ui.model.roster;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.packet.Presence;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.NetTransferMode;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SarosPluginContext.class)
public class RosterComparatorTest {

    private List<String> jids = new ArrayList<String>();

    @Before
    public void fillJids() {
        jids.clear();
        jids.add("alice@foo.com");
        jids.add("bob@foo.com");
        jids.add("carl@foo.com");
        jids.add("dave@foo.com");
        jids.add("edna@foo.com");
    }

    @Test
    public void testRosterSort() {

        PowerMock.mockStaticPartial(SarosPluginContext.class, "initComponent");

        SarosPluginContext
            .initComponent(EasyMock.isA(RosterEntryElement.class));

        PowerMock.expectLastCall().asStub();

        PowerMock.replay(SarosPluginContext.class);

        Roster mockRoster = EasyMock.createMock(Roster.class);

        EasyMock.expect(mockRoster.getEntry(EasyMock.isA(String.class)))
            .andReturn(null).anyTimes();
        EasyMock.expect(mockRoster.getPresence("alice@foo.com"))
            .andReturn(new Presence(Presence.Type.unavailable)).anyTimes();
        EasyMock.expect(mockRoster.getPresence("bob@foo.com"))
            .andReturn(new Presence(Presence.Type.unavailable)).anyTimes();
        EasyMock.expect(mockRoster.getPresence("carl@foo.com"))
            .andReturn(new Presence(Presence.Type.available)).anyTimes();
        EasyMock.expect(mockRoster.getPresence("dave@foo.com"))
            .andReturn(new Presence(Presence.Type.available)).anyTimes();
        EasyMock.expect(mockRoster.getPresence("edna@foo.com"))
            .andReturn(new Presence(Presence.Type.unavailable)).anyTimes();

        DataTransferManager dataTransferManagerMock = EasyMock
            .createMock(DataTransferManager.class);

        EasyMock
            .expect(
                dataTransferManagerMock.getTransferMode(EasyMock.isA(JID.class)))
            .andReturn(NetTransferMode.NONE).anyTimes();

        PowerMock.replayAll(mockRoster, dataTransferManagerMock);

        RosterComparator comperator = new RosterComparator();

        RosterEntryElement[] elements = RosterEntryElement.createAll(
            mockRoster, jids);

        for (RosterEntryElement element : elements)
            element.dataTransferManager = dataTransferManagerMock;

        comperator.sort(null, elements);

        PowerMock.verifyAll();

        assertEquals("carl@foo.com", elements[0].jid.getBase());

        assertEquals("dave@foo.com", elements[1].jid.getBase());

        assertEquals("alice@foo.com", elements[2].jid.getBase());

        assertEquals("bob@foo.com", elements[3].jid.getBase());

        assertEquals("edna@foo.com", elements[4].jid.getBase());

    }
}
