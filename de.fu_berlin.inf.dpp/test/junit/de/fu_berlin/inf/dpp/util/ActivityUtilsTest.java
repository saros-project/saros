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
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.TimedActivityDataObject;

public class ActivityUtilsTest {

    List<TimedActivityDataObject> timedActivities;
    SPathDataObject sPath = new SPathDataObject("", (IPath) null, "");

    @Before
    public void setUp() throws Exception {
        timedActivities = new ArrayList<TimedActivityDataObject>();

        for (int i = 5; i > 0; i--) {
            ChecksumActivityDataObject tmp = new ChecksumActivityDataObject(
                new JID(""), sPath, 0, 0);

            TimedActivityDataObject tmp1 = new TimedActivityDataObject(tmp,
                new JID(""), 0);

            timedActivities.add(tmp1);
        }

    }

    @Test
    public void testContainsChecksumsOnly0() {
        // Should be true, because there are only ChecksumActivityDataObject's
        // in the list
        assertTrue(ActivityUtils.containsChecksumsOnly(timedActivities));
    }

    @Test
    public void testContainsChecksumsOnly1() {

        List<TimedActivityDataObject> emptyTimedActivities = new ArrayList<TimedActivityDataObject>();

        // should be true, because there is no other ActivityDataObject
        assertTrue(ActivityUtils.containsChecksumsOnly(emptyTimedActivities));
    }

    @Test
    public void testContainsChecksumsOnly2() {

        ChangeColorActivityDataObject tmp = new ChangeColorActivityDataObject(
            new JID(""), new JID(""), new JID(""), 0);

        TimedActivityDataObject tmp1 = new TimedActivityDataObject(tmp,
            new JID(""), 0);
        timedActivities.add(tmp1);

        // should be false, because there is another ActivityDataObject
        assertFalse(ActivityUtils.containsChecksumsOnly(timedActivities));
    }
}
