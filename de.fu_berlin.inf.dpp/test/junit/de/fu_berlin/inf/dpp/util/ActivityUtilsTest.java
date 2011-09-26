package de.fu_berlin.inf.dpp.util;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.graphics.RGB;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.fu_berlin.inf.dpp.activities.SPathDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.ChangeColorActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.ChecksumActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.JupiterActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.TextEditActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.TextSelectionActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.ViewportActivityDataObject;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Timestamp;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.TimedActivityDataObject;

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

    @After
    public void tearDown() throws Exception {
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
            new JID(""), new JID(""), new RGB(0, 0, 0));

        TimedActivityDataObject tmp1 = new TimedActivityDataObject(tmp,
            new JID(""), 0);
        timedActivities.add(tmp1);

        // should be false, because there is another ActivityDataObject
        assertTrue(ActivityUtils.containsChecksumsOnly(timedActivities) == false);
    }

    @Test
    public void testContainsQueueableActivitiesOnly1() {
        List<IActivityDataObject> activities = new ArrayList<IActivityDataObject>();
        ChangeColorActivityDataObject tmp = new ChangeColorActivityDataObject(
            new JID(""), new JID(""), new RGB(0, 0, 0));

        activities.add(tmp);

        assertTrue(false == ActivityUtils
            .containsQueueableActivitiesOnly(activities));
    }

    @Test
    public void testContainsQueueableActivitiesOnly2() {
        List<IActivityDataObject> activities = new ArrayList<IActivityDataObject>();

        ViewportActivityDataObject tmp = new ViewportActivityDataObject(
            new JID(""), 0, 0, sPath);

        JupiterActivityDataObject tmp2 = new JupiterActivityDataObject(
            (Timestamp) null, (Operation) null, new JID(""), sPath);

        TextSelectionActivityDataObject tmp3 = new TextSelectionActivityDataObject(
            new JID(""), 0, 0, sPath);

        TextEditActivityDataObject tmp4 = new TextEditActivityDataObject(
            new JID(""), 0, "", "", sPath);

        activities.add(tmp);
        activities.add(tmp2);
        activities.add(tmp3);
        activities.add(tmp4);
        // should be true, because there are only allowed Objects in the List
        assertTrue(ActivityUtils.containsQueueableActivitiesOnly(activities));
    }

    @Test
    public void testContainsQueueableActivitiesOnly3() {
        List<IActivityDataObject> activities = new ArrayList<IActivityDataObject>();

        // should be true, because list is empty
        assertTrue(ActivityUtils.containsQueueableActivitiesOnly(activities));
    }
}
