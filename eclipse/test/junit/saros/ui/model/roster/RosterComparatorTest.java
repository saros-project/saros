package saros.ui.model.roster;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.easymock.EasyMock;
import org.eclipse.jface.viewers.StyledString;
import org.junit.Before;
import org.junit.Test;

public class RosterComparatorTest {

  /** JID and Online Status */
  private Map<String, Boolean> jids = new HashMap<>();

  @Before
  public void fillJids() {
    jids.clear();
    jids.put("alice@foo.com", false);
    jids.put("bob@foo.com", false);
    jids.put("carl@foo.com", true);
    jids.put("dave@foo.com", true);
    jids.put("edna@foo.com", false);
  }

  @Test
  public void testRosterSort() {
    RosterComparator comperator = new RosterComparator();

    RosterEntryElement[] elements = createAll(jids);

    comperator.sort(null, elements);

    assertEquals("carl@foo.com", elements[0].getStyledText().toString());
    assertEquals("dave@foo.com", elements[1].getStyledText().toString());
    assertEquals("alice@foo.com", elements[2].getStyledText().toString());
    assertEquals("bob@foo.com", elements[3].getStyledText().toString());
    assertEquals("edna@foo.com", elements[4].getStyledText().toString());
  }

  private RosterEntryElement[] createAll(Map<String, Boolean> addresses) {
    List<RosterEntryElement> rosterEntryElements = new ArrayList<RosterEntryElement>();
    addresses.forEach(
        (address, status) -> {
          RosterEntryElement entry = EasyMock.createNiceMock(RosterEntryElement.class);
          EasyMock.expect(entry.getStyledText()).andStubReturn(new StyledString(address));
          EasyMock.expect(entry.isOnline()).andStubReturn(status);
          EasyMock.replay(entry);

          rosterEntryElements.add(entry);
        });
    return rosterEntryElements.toArray(new RosterEntryElement[0]);
  }
}
