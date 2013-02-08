package de.fu_berlin.inf.dpp.editor.colorstorage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.junit.Before;
import org.junit.Test;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.test.util.MemoryPreferenceStore;

public class ColorIDSetStorageTest {

    private List<JID> jids;
    private JID alice, bob, carl, dave;

    private IPreferenceStore preferenceStore;

    private ColorIDSetStorage storage;

    @Before
    public void setUp() {
        jids = new ArrayList<JID>();
        alice = new JID("alice@saros.org/Wunderland");
        jids.add(alice);
        bob = new JID("bob@saros.org/Jamaica");
        jids.add(bob);
        carl = new JID("carl@lagerfeld.org/Paris");
        jids.add(carl);
        dave = new JID("dave@saros.org/Hell");
        jids.add(dave);

        preferenceStore = new MemoryPreferenceStore();
    }

    @Test
    public void testGet() {
        storage = new ColorIDSetStorage(preferenceStore);

        ColorIDSet set0 = storage.getColorIDSet(jids);
        assertEquals(1, storage.size());

        ColorIDSet set1 = storage.getColorIDSet(jids);
        assertEquals(1, storage.size());

        assertEquals("a new set was returned", set0, set1);

        jids.remove(dave);
        storage.getColorIDSet(jids);
        assertEquals(2, storage.size());
    }

    @Test
    public void testExtend() {
        List<JID> newJIDs = new ArrayList<JID>();
        JID a = new JID("a@a/a");
        JID b = new JID("b@b/b");
        newJIDs.add(a);
        newJIDs.add(b);

        storage = new ColorIDSetStorage(preferenceStore);

        storage.getColorIDSet(jids);
        storage.getColorIDSet(jids, newJIDs);
        assertEquals(2, storage.size());

        storage.getColorIDSet(jids, newJIDs);
        assertEquals(2, storage.size());
    }

    @Test
    public void testRemove() {
        storage = new ColorIDSetStorage(preferenceStore);

        storage.getColorIDSet(jids);
        storage.remove(0);
        assertEquals("Storage did not remove entries.", 0, storage.size());

        storage.getColorIDSet(jids);
        assertEquals("Storage should not be empty.", 1, storage.size());

        storage.remove(Long.MAX_VALUE);
        assertEquals("Storage should not be empty.", 1, storage.size());
    }

    @Test
    public void testUpdateColor() {
        storage = new ColorIDSetStorage(preferenceStore);

        ColorIDSet set = storage.getColorIDSet(jids);

        storage.updateColor(set, alice, 0);
        assertEquals("Initial value of Alice's has changed.", 0,
            set.getColorID(alice));

        storage.updateColor(set, alice, 4);
        assertEquals("New color has not been applied.", 4,
            set.getColorID(alice));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateColorWithOccupiedColor() {
        storage = new ColorIDSetStorage(preferenceStore);

        ColorIDSet set = storage.getColorIDSet(jids);

        // init
        storage.updateColor(set, bob, 1);

        // set Alice to an already occupied color
        storage.updateColor(set, alice, 1);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateColorOfNonExistingJID() {
        storage = new ColorIDSetStorage(preferenceStore);

        ColorIDSet set = storage.getColorIDSet(jids);

        storage.updateColor(set, new JID("foo@bar"), 1);
    }

    @Test
    public void testPersistence() {
        storage = new ColorIDSetStorage(preferenceStore);
        assertEquals("Loading resulted in an unexpected set.", 0,
            storage.size());

        /* The following sets should be saved implicitly. */
        ColorIDSet colorIDSet = storage.getColorIDSet(jids);
        ColorIDSet singleColorIDSet = storage.getColorIDSet(Collections
            .singletonList(alice));

        /* The following sets should be saved implicitly. */
        assertEquals("Sets have not been added.", 2, storage.size());

        /* Create new instance with the "old" data */
        storage = new ColorIDSetStorage(preferenceStore);
        assertEquals("Load did not load expected amount of sets.", 2,
            storage.size());

        ColorIDSet loadedColorIDSet = storage.getColorIDSet(jids);
        assertNotNull("Loaded set could not be retrieved.", loadedColorIDSet);
        assertEquals("Newly loaded set does not match loaded one.", colorIDSet,
            loadedColorIDSet);

        loadedColorIDSet = storage.getColorIDSet(Collections
            .singletonList(alice));

        assertNotNull("Loaded set could not be retrieved.", loadedColorIDSet);

        assertEquals("Newly loaded set does not match loaded one.",
            singleColorIDSet, loadedColorIDSet);
    }

}
