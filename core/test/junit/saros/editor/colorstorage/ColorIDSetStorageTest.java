package saros.editor.colorstorage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import saros.preferences.IPreferenceStore;
import saros.test.util.MemoryPreferenceStore;

public class ColorIDSetStorageTest {

  private List<String> ids;
  private String alice, bob, carl, dave;

  private IPreferenceStore preferenceStore;

  private ColorIDSetStorage storage;

  @Before
  public void setUp() {
    ids = new ArrayList<String>();
    alice = "alice@saros.org";
    ids.add(alice);
    bob = "bob@saros.org";
    ids.add(bob);
    carl = "carl@lagerfeld.org";
    ids.add(carl);
    dave = "dave@saros.org";
    ids.add(dave);

    preferenceStore = new MemoryPreferenceStore();
  }

  @Test
  public void testGet() {
    storage = new ColorIDSetStorage(preferenceStore);

    ColorIDSet set0 = storage.getColorIDSet(ids);
    assertEquals(1, storage.size());

    ColorIDSet set1 = storage.getColorIDSet(ids);
    assertEquals(1, storage.size());

    assertEquals("a new set was returned", set0, set1);

    ids.remove(dave);
    storage.getColorIDSet(ids);
    assertEquals(2, storage.size());
  }

  @Test
  public void testExtend() {
    List<String> newIDs = Arrays.asList(new String[] {"a@a/a", "b@b/b"});

    storage = new ColorIDSetStorage(preferenceStore);

    storage.getColorIDSet(ids);
    storage.getColorIDSet(ids, newIDs);
    assertEquals(2, storage.size());

    storage.getColorIDSet(ids, newIDs);
    assertEquals(2, storage.size());
  }

  @Test
  public void testRemove() {
    storage = new ColorIDSetStorage(preferenceStore);

    storage.getColorIDSet(ids);
    storage.remove(0);
    assertEquals("Storage did not remove entries.", 0, storage.size());

    storage.getColorIDSet(ids);
    assertEquals("Storage should not be empty.", 1, storage.size());

    storage.remove(Long.MAX_VALUE);
    assertEquals("Storage should not be empty.", 1, storage.size());
  }

  @Test
  public void testUpdateColor() {
    storage = new ColorIDSetStorage(preferenceStore);

    ColorIDSet set = storage.getColorIDSet(ids);

    storage.updateColor(set, alice, 0, -1);
    assertEquals("Initial value of Alice's has changed.", 0, set.getColor(alice));

    storage.updateColor(set, alice, 4, -1);
    assertEquals("New color has not been applied.", 4, set.getColor(alice));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUpdateColorWithOccupiedColor() {
    storage = new ColorIDSetStorage(preferenceStore);

    ColorIDSet set = storage.getColorIDSet(ids);

    // init
    storage.updateColor(set, bob, 1, -1);

    // set Alice to an already occupied color
    storage.updateColor(set, alice, 1, -1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUpdateColorOfNonExistingJID() {
    storage = new ColorIDSetStorage(preferenceStore);

    ColorIDSet set = storage.getColorIDSet(ids);

    storage.updateColor(set, "foo@bar", 1, -1);
  }

  @Test
  public void testPersistence() {
    storage = new ColorIDSetStorage(preferenceStore);
    assertEquals("Loading resulted in an unexpected set.", 0, storage.size());

    /* The following sets should be saved implicitly. */
    ColorIDSet colorIDSet = storage.getColorIDSet(ids);
    ColorIDSet singleColorIDSet = storage.getColorIDSet(Collections.singletonList(alice));

    /* The following sets should be saved implicitly. */
    assertEquals("Sets have not been added.", 2, storage.size());

    /* Create new instance with the "old" data */
    storage = new ColorIDSetStorage(preferenceStore);
    assertEquals("Load did not load expected amount of sets.", 2, storage.size());

    ColorIDSet loadedColorIDSet = storage.getColorIDSet(ids);
    assertNotNull("Loaded set could not be retrieved.", loadedColorIDSet);
    assertEquals("Newly loaded set does not match loaded one.", colorIDSet, loadedColorIDSet);

    loadedColorIDSet = storage.getColorIDSet(Collections.singletonList(alice));

    assertNotNull("Loaded set could not be retrieved.", loadedColorIDSet);

    assertEquals("Newly loaded set does not match loaded one.", singleColorIDSet, loadedColorIDSet);
  }
}
