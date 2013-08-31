package de.fu_berlin.inf.dpp.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.junit.Before;
import org.junit.Test;

import de.fu_berlin.inf.dpp.activities.SPathDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.ChangeColorActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.ChecksumActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.net.JID;

public class ActivityUtilsTest {

    List<IActivityDataObject> activities;
    SPathDataObject sPath = new SPathDataObject("", (IPath) null, "");

    @Before
    public void setUp() throws Exception {
        activities = new ArrayList<IActivityDataObject>();

        for (int i = 5; i > 0; i--) {
            ChecksumActivityDataObject tmp = new ChecksumActivityDataObject(
                new JID(""), sPath, 0, 0, null);

            activities.add(tmp);
        }

    }

    @Test
    public void testContainsChecksumsOnly0() {
        // Should be true, because there are only ChecksumActivityDataObject's
        // in the list
        assertTrue(ActivityUtils.containsChecksumsOnly(activities));
    }

    @Test
    public void testContainsChecksumsOnly1() {

        List<IActivityDataObject> emptyTimedActivities = new ArrayList<IActivityDataObject>();

        // should be true, because there is no other ActivityDataObject
        assertTrue(ActivityUtils.containsChecksumsOnly(emptyTimedActivities));
    }

    @Test
    public void testContainsChecksumsOnly2() {

        ChangeColorActivityDataObject tmp = new ChangeColorActivityDataObject(
            new JID(""), new JID(""), new JID(""), 0);

        activities.add(tmp);

        // should be false, because there is another ActivityDataObject
        assertFalse(ActivityUtils.containsChecksumsOnly(activities));
    }
}
