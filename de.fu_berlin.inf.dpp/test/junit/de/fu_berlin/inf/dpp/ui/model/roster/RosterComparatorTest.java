package de.fu_berlin.inf.dpp.ui.model.roster;

import static org.junit.Assert.assertEquals;

import de.fu_berlin.inf.dpp.net.xmpp.JID;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.easymock.EasyMock;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.packet.Presence;
import org.junit.Before;
import org.junit.Test;

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

    Roster mockRoster = EasyMock.createMock(Roster.class);

    EasyMock.expect(mockRoster.getEntry(EasyMock.isA(String.class))).andStubReturn(null);

    EasyMock.expect(mockRoster.getPresence("alice@foo.com"))
        .andStubReturn(new Presence(Presence.Type.unavailable));

    EasyMock.expect(mockRoster.getPresence("bob@foo.com"))
        .andStubReturn(new Presence(Presence.Type.unavailable));

    EasyMock.expect(mockRoster.getPresence("carl@foo.com"))
        .andStubReturn(new Presence(Presence.Type.available));

    EasyMock.expect(mockRoster.getPresence("dave@foo.com"))
        .andStubReturn(new Presence(Presence.Type.available));

    EasyMock.expect(mockRoster.getPresence("edna@foo.com"))
        .andStubReturn(new Presence(Presence.Type.unavailable));

    EasyMock.replay(mockRoster);

    RosterComparator comperator = new RosterComparator();

    RosterEntryElement[] elements = createAll(mockRoster, jids);

    comperator.sort(null, elements);

    EasyMock.verify(mockRoster);

    assertEquals("carl@foo.com", elements[0].getJID().getBase());

    assertEquals("dave@foo.com", elements[1].getJID().getBase());

    assertEquals("alice@foo.com", elements[2].getJID().getBase());

    assertEquals("bob@foo.com", elements[3].getJID().getBase());

    assertEquals("edna@foo.com", elements[4].getJID().getBase());
  }

  private RosterEntryElement[] createAll(Roster roster, Collection<String> addresses) {
    List<RosterEntryElement> rosterEntryElements = new ArrayList<RosterEntryElement>();
    for (Iterator<String> iterator = addresses.iterator(); iterator.hasNext(); ) {
      String address = iterator.next();
      rosterEntryElements.add(new RosterEntryElement(roster, new JID(address), true));
    }
    return rosterEntryElements.toArray(new RosterEntryElement[0]);
  }
}
