package saros.editor.colorstorage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import saros.net.xmpp.JID;
import saros.session.User;

public class ColorIDSetTest {

  private List<User> userList;
  private Map<String, UserColorID> userMap;
  private User alice, bob, carl, dave;

  @Before
  public void setUp() {
    userList = new ArrayList<User>();
    userMap = new HashMap<String, UserColorID>();

    alice = new User(new JID("alice@saros.org"), false, false, 0, -1);
    bob = new User(new JID("bob@saros.org"), false, false, 1, -1);
    carl = new User(new JID("carl@lagerfeld.org"), false, false, 2, -1);
    dave = new User(new JID("dave@saros.org"), false, false, 0, -1);
  }

  private ColorIDSet createColorIDSet(Collection<User> users) {
    Set<String> ids = new HashSet<String>();

    for (User user : users) ids.add(user.getJID().toString());

    ColorIDSet set = new ColorIDSet(ids);

    for (User user : users) set.setColor(user.getJID().toString(), user.getColorID());

    return set;
  }

  @Test(expected = IllegalArgumentException.class)
  public void testJIDConstructor() {
    ColorIDSet set;

    set = createColorIDSet(userList);
    checkConsistency(set);

    addUser(alice);
    addUser(bob);
    addUser(carl);

    set = createColorIDSet(userList);
    checkConsistency(set);

    addUser(dave);
    set = createColorIDSet(userList);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUserConstructor() {
    ColorIDSet set;

    set = createColorIDSet(userList);
    checkConsistency(set);

    addUser(alice);
    addUser(bob);
    addUser(carl);

    set = new ColorIDSet(userMap);
    checkConsistency(set);

    addUser(dave);
    set = createColorIDSet(userList);
  }

  @Test
  public void testNonexistantID() {
    ColorIDSet set = createColorIDSet(userList);
    assertEquals(
        "Expected -1 as the user does not exist in the ColorIdSet.",
        -1,
        set.getColor("satan@hell.com"));
  }

  @Test
  public void testIsAvailable() {
    addUser(alice);
    ColorIDSet set = createColorIDSet(userList);

    assertFalse("0 should not be available as it is used by Alice", set.isAvailable(0));
    for (int i = 1; i < 5; i++) {
      assertTrue(i + " should be available.", set.isAvailable(i));
    }
  }

  @Test
  public void testGetFavoriteColor() {
    addUser(alice);
    ColorIDSet set = createColorIDSet(userList);

    set.setFavoriteColor(alice.getJID().toString(), 4);

    assertEquals(
        "Alice favorite color does not match", 4, set.getFavoriteColor(alice.getJID().toString()));
  }

  @Test
  public void testResetTimestamp() {

    addUser(alice);
    ColorIDSet set = createColorIDSet(userList);

    set.setTimestamp(Long.MAX_VALUE);
    set.resetTimestamp();

    assertTrue("timestamp was not reset", set.getTimestamp() < Long.MAX_VALUE);
  }

  private void checkConsistency(ColorIDSet set) {
    Collection<String> users = set.getParticipants();

    for (User user : userList) {
      assertEquals(
          "Color IDs do not match.", user.getColorID(), set.getColor(user.getJID().toString()));

      assertTrue(
          "User is not contained in the sets' participants: " + user.getJID().toString(),
          users.contains(user.getJID().toString()));
    }
  }

  private void addUser(User user) {
    userList.add(user);
    userMap.put(user.getJID().toString(), new UserColorID(user.getColorID(), -1));
  }
}
