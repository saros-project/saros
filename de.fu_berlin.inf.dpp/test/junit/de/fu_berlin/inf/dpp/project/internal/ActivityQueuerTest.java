package de.fu_berlin.inf.dpp.project.internal;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Test;

import de.fu_berlin.inf.dpp.activities.SPathDataObject;
import de.fu_berlin.inf.dpp.activities.business.EditorActivity;
import de.fu_berlin.inf.dpp.activities.serializable.EditorActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.JupiterActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.NOPActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.StartFollowingActivityDataObject;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.JupiterVectorTime;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.NoOperation;
import de.fu_berlin.inf.dpp.net.JID;

public class ActivityQueuerTest {

    private static final JID ALICE = new JID("Alice");
    private static final JID BOB = new JID("Bob");
    private static final String SHARED_PROJECT_ID = "1234";
    private static final String NOT_SHARED_PROJECT_ID = "4321";

    private static final SPathDataObject FOO_PATH_SHARED_PROJECT = new SPathDataObject(
        SHARED_PROJECT_ID, Path.fromOSString("foo"), "editorType");

    private static final SPathDataObject BAR_PATH_SHARED_PROJECT = new SPathDataObject(
        SHARED_PROJECT_ID, Path.fromOSString("bar"), "editorType");

    private static final SPathDataObject PATH_TO_NOT_SHARED_PROJECT = new SPathDataObject(
        NOT_SHARED_PROJECT_ID, Path.fromOSString("unshared"), "editorType");

    private ActivityQueuer activityQueuer;

    @Before
    public void setUp() {
        activityQueuer = new ActivityQueuer();
    }

    @Test
    public void testQueuingDisabled() {
        List<IActivityDataObject> activities = createSomeActivities();

        List<IActivityDataObject> processedActivities = activityQueuer
            .process(activities);

        assertListsAreEqual(activities, processedActivities);
    }

    @Test
    public void testQueuingEnabled() {
        List<IActivityDataObject> activities = createSomeActivities();

        List<IActivityDataObject> expectedActivities = new ArrayList<IActivityDataObject>(
            activities);

        // see testHackForBug808 ... we are generating missing ADOs if necessary
        final IActivityDataObject expectedEditorADO = new EditorActivityDataObject(
            BOB, EditorActivity.Type.ACTIVATED, PATH_TO_NOT_SHARED_PROJECT);

        IActivityDataObject activityToBeQueued = createJupiterActivity(PATH_TO_NOT_SHARED_PROJECT);
        activities.add(activityToBeQueued);

        activityQueuer.enableQueuing(NOT_SHARED_PROJECT_ID);

        List<IActivityDataObject> processedActivities = activityQueuer
            .process(activities);

        assertFalse(processedActivities.contains(activityToBeQueued));
        assertListsAreEqual(expectedActivities, processedActivities);

        IActivityDataObject activityNotToBeQueued = createJupiterActivity(FOO_PATH_SHARED_PROJECT);

        List<IActivityDataObject> notQueuedActivities = activityQueuer
            .process(Collections.singletonList(activityNotToBeQueued));

        assertTrue("queued activity that should not be queued",
            notQueuedActivities.contains(activityNotToBeQueued));

        assertEquals("queued activities were prematurly flushed", 1,
            notQueuedActivities.size());

        // flush queue
        IActivityDataObject nopActivity = new NOPActivityDataObject(ALICE,
            ALICE, 0);

        activityQueuer.disableQueuing();

        processedActivities.addAll(activityQueuer.process(Collections
            .singletonList(nopActivity)));

        expectedActivities.add(expectedEditorADO);
        expectedActivities.add(activityToBeQueued);
        expectedActivities.add(nopActivity);
        assertListsAreEqual(expectedActivities, processedActivities);

        // ensure that queued activities are removed after flush
        processedActivities = activityQueuer.process(activities);
        assertListsAreEqual(activities, processedActivities);
    }

    @Test
    public void testQueuingEnabledWithActivityWithoutPath() {
        activityQueuer.enableQueuing(NOT_SHARED_PROJECT_ID);

        IActivityDataObject serializedEditorActivity = new EditorActivityDataObject(
            ALICE, EditorActivity.Type.ACTIVATED, null);

        /*
         * does this make sense ? user opened file X (path != null), closed file
         * X (path == null), opened file Y(path != null) result = X closed, X
         * opened Y opened See EditorManager#partActivated
         */

        List<IActivityDataObject> processedActivities = activityQueuer
            .process(Collections.singletonList(serializedEditorActivity));

        assertEquals("activities with null path must not be queued", 1,
            processedActivities.size());

    }

    // http://sourceforge.net/p/dpp/bugs/808/
    @Test
    public void testHackForBug808() {
        final IActivityDataObject fooExpectedEditorADO = new EditorActivityDataObject(
            ALICE, EditorActivity.Type.ACTIVATED, FOO_PATH_SHARED_PROJECT);

        final IActivityDataObject barExpectedEditorADO = new EditorActivityDataObject(
            ALICE, EditorActivity.Type.ACTIVATED, BAR_PATH_SHARED_PROJECT);

        final IActivityDataObject fooClosedEditorADO = new EditorActivityDataObject(
            ALICE, EditorActivity.Type.CLOSED, FOO_PATH_SHARED_PROJECT);

        final IActivityDataObject fooSavedEditorADO = new EditorActivityDataObject(
            ALICE, EditorActivity.Type.CLOSED, FOO_PATH_SHARED_PROJECT);

        final List<IActivityDataObject> flush = Collections.emptyList();

        final IActivityDataObject fooJupiterADO = new JupiterActivityDataObject(
            new JupiterVectorTime(0, 0), new NoOperation(), ALICE,
            FOO_PATH_SHARED_PROJECT);

        final IActivityDataObject barJupiterADO = new JupiterActivityDataObject(
            new JupiterVectorTime(0, 0), new NoOperation(), ALICE,
            BAR_PATH_SHARED_PROJECT);

        List<IActivityDataObject> ados;

        activityQueuer = new ActivityQueuer();
        activityQueuer.enableQueuing(SHARED_PROJECT_ID);

        activityQueuer.process(Collections.singletonList(fooJupiterADO));
        activityQueuer.disableQueuing();

        ados = activityQueuer.process(flush);

        IActivityDataObject a = ados.get(0);

        fooExpectedEditorADO.equals(a);

        assertEquals("editor ADO was not added", 2, ados.size());

        assertEquals("wrong (Editor)ADO was inserted", fooExpectedEditorADO,
            ados.get(0));

        ados.clear();

        // ------------------------------------------

        activityQueuer = new ActivityQueuer();
        activityQueuer.enableQueuing(SHARED_PROJECT_ID);

        activityQueuer.process(Collections.singletonList(fooClosedEditorADO));
        activityQueuer.disableQueuing();

        ados = activityQueuer.process(flush);

        assertEquals("editor ADO was not added", 2, ados.size());

        assertEquals("wrong (Editor)ADO was inserted", fooExpectedEditorADO,
            ados.get(0));

        ados.clear();

        // ------------------------------------------

        activityQueuer = new ActivityQueuer();
        activityQueuer.enableQueuing(SHARED_PROJECT_ID);

        activityQueuer.process(Collections.singletonList(fooSavedEditorADO));
        activityQueuer.disableQueuing();

        ados = activityQueuer.process(flush);

        assertEquals("editor ADO was not added", 2, ados.size());

        assertEquals("wrong (Editor)ADO was inserted", fooExpectedEditorADO,
            ados.get(0));

        ados.clear();

        // ------------------------------------------

        activityQueuer = new ActivityQueuer();
        activityQueuer.enableQueuing(SHARED_PROJECT_ID);

        activityQueuer.process(Arrays.asList(fooJupiterADO, fooSavedEditorADO,
            fooClosedEditorADO));
        activityQueuer.disableQueuing();

        ados = activityQueuer.process(flush);

        assertEquals("editor ADO was either not added or added to many times",
            4, ados.size());

        assertEquals("wrong (Editor)ADO was inserted", fooExpectedEditorADO,
            ados.get(0));

        ados.clear();

        // ------------------------------------------

        activityQueuer = new ActivityQueuer();
        activityQueuer.enableQueuing(SHARED_PROJECT_ID);

        activityQueuer.process(Arrays.asList(fooJupiterADO, barJupiterADO));
        activityQueuer.disableQueuing();

        ados = activityQueuer.process(flush);

        assertEquals("editor ADO was not added two times", 4, ados.size());

        assertEquals("wrong (Editor)ADO was inserted", fooExpectedEditorADO,
            ados.get(0));

        assertEquals("wrong (Editor)ADO was inserted", barExpectedEditorADO,
            ados.get(2));

        ados.clear();

        // ------------------------------------------

        final IActivityDataObject aliceExpectedEditorADO = new EditorActivityDataObject(
            ALICE, EditorActivity.Type.ACTIVATED, FOO_PATH_SHARED_PROJECT);

        final IActivityDataObject bobExpectedEditorADO = new EditorActivityDataObject(
            BOB, EditorActivity.Type.ACTIVATED, FOO_PATH_SHARED_PROJECT);

        final IActivityDataObject aliceJupiterADO = new JupiterActivityDataObject(
            new JupiterVectorTime(0, 0), new NoOperation(), ALICE,
            FOO_PATH_SHARED_PROJECT);

        final IActivityDataObject bobJupiterADO = new JupiterActivityDataObject(
            new JupiterVectorTime(0, 0), new NoOperation(), BOB,
            FOO_PATH_SHARED_PROJECT);

        activityQueuer = new ActivityQueuer();
        activityQueuer.enableQueuing(SHARED_PROJECT_ID);

        activityQueuer.process(Arrays.asList(aliceJupiterADO, bobJupiterADO));
        activityQueuer.disableQueuing();

        ados = activityQueuer.process(flush);

        assertEquals("editor ADO was not added two times", 4, ados.size());

        assertEquals("wrong (Editor)ADO was inserted", aliceExpectedEditorADO,
            ados.get(0));

        assertEquals("wrong (Editor)ADO was inserted", bobExpectedEditorADO,
            ados.get(2));
    }

    private List<IActivityDataObject> createSomeActivities() {
        IActivityDataObject startFollowingActivity = new StartFollowingActivityDataObject(
            ALICE, BOB);

        IActivityDataObject jupiterActivity = createJupiterActivity(FOO_PATH_SHARED_PROJECT);

        List<IActivityDataObject> activities = new ArrayList<IActivityDataObject>();
        activities.add(startFollowingActivity);
        activities.add(jupiterActivity);

        return activities;
    }

    private JupiterActivityDataObject createJupiterActivity(SPathDataObject path) {
        return new JupiterActivityDataObject(new JupiterVectorTime(0, 0),
            new NoOperation(), BOB, path);
    }

    private void assertListsAreEqual(List<IActivityDataObject> expected,
        List<IActivityDataObject> actual) {
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), actual.get(i));
        }
    }

}
