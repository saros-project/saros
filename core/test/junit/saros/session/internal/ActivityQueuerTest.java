package saros.session.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import saros.activities.EditorActivity;
import saros.activities.FolderCreatedActivity;
import saros.activities.FolderDeletedActivity;
import saros.activities.IActivity;
import saros.activities.JupiterActivity;
import saros.activities.NOPActivity;
import saros.activities.StartFollowingActivity;
import saros.concurrent.jupiter.internal.JupiterVectorTime;
import saros.concurrent.jupiter.internal.text.NoOperation;
import saros.filesystem.IFile;
import saros.filesystem.IFolder;
import saros.filesystem.IReferencePoint;
import saros.net.xmpp.JID;
import saros.session.User;

public class ActivityQueuerTest {

  private static final User ALICE = new User(new JID("Alice"), true, true, null);
  private static final User BOB = new User(new JID("Bob"), false, false, null);

  private static IReferencePoint SHARED_PROJECT;
  private static IReferencePoint NOT_SHARED_PROJECT;

  private static IFile FOO_FILE_SHARED_PROJECT;
  private static IFile BAR_FILE_SHARED_PROJECT;
  private static IFile FILE_OF_NOT_SHARED_PROJECT;
  private static IFolder FOLDER_OF_NOT_SHARED_PROJECT;

  private ActivityQueuer activityQueuer;

  @BeforeClass
  public static void prepare() {
    SHARED_PROJECT = EasyMock.createMock(IReferencePoint.class);
    NOT_SHARED_PROJECT = EasyMock.createMock(IReferencePoint.class);

    FOO_FILE_SHARED_PROJECT = EasyMock.createNiceMock(IFile.class);
    EasyMock.expect(FOO_FILE_SHARED_PROJECT.getReferencePoint()).andStubReturn(SHARED_PROJECT);

    BAR_FILE_SHARED_PROJECT = EasyMock.createNiceMock(IFile.class);
    EasyMock.expect(BAR_FILE_SHARED_PROJECT.getReferencePoint()).andStubReturn(SHARED_PROJECT);

    FILE_OF_NOT_SHARED_PROJECT = EasyMock.createNiceMock(IFile.class);
    EasyMock.expect(FILE_OF_NOT_SHARED_PROJECT.getReferencePoint())
        .andStubReturn(NOT_SHARED_PROJECT);

    FOLDER_OF_NOT_SHARED_PROJECT = EasyMock.createNiceMock(IFolder.class);
    EasyMock.expect(FOLDER_OF_NOT_SHARED_PROJECT.getReferencePoint())
        .andStubReturn(NOT_SHARED_PROJECT);

    EasyMock.replay(
        FOO_FILE_SHARED_PROJECT,
        BAR_FILE_SHARED_PROJECT,
        FILE_OF_NOT_SHARED_PROJECT,
        FOLDER_OF_NOT_SHARED_PROJECT);

    EasyMock.replay(SHARED_PROJECT, NOT_SHARED_PROJECT);
  }

  @Before
  public void setUp() {
    activityQueuer = new ActivityQueuer();
  }

  @Test
  public void testQueuingDisabled() {
    List<IActivity> activities = createSomeActivities();

    List<IActivity> processedActivities = activityQueuer.process(activities);

    assertListsAreEqual(activities, processedActivities);
  }

  @Test
  public void testQueuingEnabled() {
    List<IActivity> activities = createSomeActivities();

    List<IActivity> expectedActivities = new ArrayList<IActivity>(activities);

    // see testHackForBug808 ... we are generating missing activities if
    // necessary
    final IActivity expectedEA =
        new EditorActivity(BOB, EditorActivity.Type.ACTIVATED, FILE_OF_NOT_SHARED_PROJECT);

    IActivity activityToBeQueued = createJupiterActivity(FILE_OF_NOT_SHARED_PROJECT);
    activities.add(activityToBeQueued);

    activityQueuer.enableQueuing(NOT_SHARED_PROJECT);

    List<IActivity> processedActivities = activityQueuer.process(activities);

    assertFalse(processedActivities.contains(activityToBeQueued));
    assertListsAreEqual(expectedActivities, processedActivities);

    IActivity activityNotToBeQueued = createJupiterActivity(FOO_FILE_SHARED_PROJECT);

    List<IActivity> notQueuedActivities =
        activityQueuer.process(Collections.singletonList(activityNotToBeQueued));

    assertTrue(
        "queued activity that should not be queued",
        notQueuedActivities.contains(activityNotToBeQueued));

    assertEquals("queued activities were prematurly flushed", 1, notQueuedActivities.size());

    // flush queue
    IActivity nopActivity = new NOPActivity(ALICE, ALICE, 0);

    activityQueuer.disableQueuing(NOT_SHARED_PROJECT);

    processedActivities.addAll(activityQueuer.process(Collections.singletonList(nopActivity)));

    expectedActivities.add(expectedEA);
    expectedActivities.add(activityToBeQueued);
    expectedActivities.add(nopActivity);
    assertListsAreEqual(expectedActivities, processedActivities);

    // ensure that queued activities are removed after flush
    processedActivities = activityQueuer.process(activities);
    assertListsAreEqual(activities, processedActivities);
  }

  @Test
  public void testQueuingEnabledWithActivityWithoutFile() {
    activityQueuer.enableQueuing(NOT_SHARED_PROJECT);

    IActivity serializedEditorActivity =
        new EditorActivity(ALICE, EditorActivity.Type.ACTIVATED, null);

    /*
     * does this make sense ? user opened file X (file != null), closed file
     * X (file == null), opened file Y(file != null) result = X closed, X
     * opened Y opened See EditorManager#partActivated
     */

    List<IActivity> processedActivities =
        activityQueuer.process(Collections.singletonList(serializedEditorActivity));

    assertEquals("activities with null file must not be queued", 1, processedActivities.size());
  }

  @Test
  public void testInternalFushCounter() {
    activityQueuer.enableQueuing(NOT_SHARED_PROJECT);
    activityQueuer.enableQueuing(NOT_SHARED_PROJECT);

    IActivity firstActivityToBeQueued =
        new FolderCreatedActivity(BOB, FOLDER_OF_NOT_SHARED_PROJECT);

    List<IActivity> result;

    result = activityQueuer.process(Collections.singletonList(firstActivityToBeQueued));

    assertEquals("activity was not queued", 0, result.size());

    IActivity secondActivityToBeQueued =
        new FolderDeletedActivity(ALICE, FOLDER_OF_NOT_SHARED_PROJECT);

    activityQueuer.disableQueuing(NOT_SHARED_PROJECT);

    result = activityQueuer.process(Collections.singletonList(secondActivityToBeQueued));

    assertEquals("activity was not queued", 0, result.size());

    activityQueuer.disableQueuing(NOT_SHARED_PROJECT);

    result = activityQueuer.process(Collections.<IActivity>emptyList());

    assertEquals("not all activities were flushed", 2, result.size());

    assertSame("wrong flushing order", firstActivityToBeQueued, result.get(0));

    assertSame("wrong flushing order", secondActivityToBeQueued, result.get(1));

    IActivity activityNotToBeQueued =
        new FolderDeletedActivity(ALICE, FOLDER_OF_NOT_SHARED_PROJECT);

    result = activityQueuer.process(Collections.singletonList(activityNotToBeQueued));

    assertEquals("activities were queued", 1, result.size());

    assertSame("wrong activitiy return", activityNotToBeQueued, result.get(0));
  }

  // http://sourceforge.net/p/dpp/bugs/808/
  @Test
  public void testHackForBug808() {
    final IActivity fooExpectedEditorADO =
        new EditorActivity(ALICE, EditorActivity.Type.ACTIVATED, FOO_FILE_SHARED_PROJECT);

    final IActivity barExpectedEditorADO =
        new EditorActivity(ALICE, EditorActivity.Type.ACTIVATED, BAR_FILE_SHARED_PROJECT);

    final IActivity fooClosedEditorADO =
        new EditorActivity(ALICE, EditorActivity.Type.CLOSED, FOO_FILE_SHARED_PROJECT);

    final IActivity fooSavedEditorADO =
        new EditorActivity(ALICE, EditorActivity.Type.CLOSED, FOO_FILE_SHARED_PROJECT);

    final List<IActivity> flush = Collections.emptyList();

    final IActivity fooJupiterADO =
        new JupiterActivity(
            new JupiterVectorTime(0, 0), new NoOperation(), ALICE, FOO_FILE_SHARED_PROJECT);

    final IActivity barJupiterADO =
        new JupiterActivity(
            new JupiterVectorTime(0, 0), new NoOperation(), ALICE, BAR_FILE_SHARED_PROJECT);

    List<IActivity> activities;

    activityQueuer = new ActivityQueuer();
    activityQueuer.enableQueuing(SHARED_PROJECT);

    activityQueuer.process(Collections.singletonList(fooJupiterADO));
    activityQueuer.disableQueuing(SHARED_PROJECT);

    activities = activityQueuer.process(flush);

    IActivity a = activities.get(0);

    fooExpectedEditorADO.equals(a);

    assertEquals("editor ADO was not added", 2, activities.size());

    assertEquals("wrong (Editor)ADO was inserted", fooExpectedEditorADO, activities.get(0));

    activities.clear();

    // ------------------------------------------

    activityQueuer = new ActivityQueuer();
    activityQueuer.enableQueuing(SHARED_PROJECT);

    activityQueuer.process(Collections.singletonList(fooClosedEditorADO));
    activityQueuer.disableQueuing(SHARED_PROJECT);

    activities = activityQueuer.process(flush);

    assertEquals("editor ADO was not added", 2, activities.size());

    assertEquals("wrong (Editor)ADO was inserted", fooExpectedEditorADO, activities.get(0));

    activities.clear();

    // ------------------------------------------

    activityQueuer = new ActivityQueuer();
    activityQueuer.enableQueuing(SHARED_PROJECT);

    activityQueuer.process(Collections.singletonList(fooSavedEditorADO));
    activityQueuer.disableQueuing(SHARED_PROJECT);

    activities = activityQueuer.process(flush);

    assertEquals("editor ADO was not added", 2, activities.size());

    assertEquals("wrong (Editor)ADO was inserted", fooExpectedEditorADO, activities.get(0));

    activities.clear();

    // ------------------------------------------

    activityQueuer = new ActivityQueuer();
    activityQueuer.enableQueuing(SHARED_PROJECT);

    activityQueuer.process(Arrays.asList(fooJupiterADO, fooSavedEditorADO, fooClosedEditorADO));
    activityQueuer.disableQueuing(SHARED_PROJECT);

    activities = activityQueuer.process(flush);

    assertEquals("editor ADO was either not added or added to many times", 4, activities.size());

    assertEquals("wrong (Editor)ADO was inserted", fooExpectedEditorADO, activities.get(0));

    activities.clear();

    // ------------------------------------------

    activityQueuer = new ActivityQueuer();
    activityQueuer.enableQueuing(SHARED_PROJECT);

    activityQueuer.process(Arrays.asList(fooJupiterADO, barJupiterADO));
    activityQueuer.disableQueuing(SHARED_PROJECT);

    activities = activityQueuer.process(flush);

    assertEquals("editor ADO was not added two times", 4, activities.size());

    assertEquals("wrong (Editor)ADO was inserted", fooExpectedEditorADO, activities.get(0));

    assertEquals("wrong (Editor)ADO was inserted", barExpectedEditorADO, activities.get(2));

    activities.clear();

    // ------------------------------------------

    final IActivity aliceExpectedEditorADO =
        new EditorActivity(ALICE, EditorActivity.Type.ACTIVATED, FOO_FILE_SHARED_PROJECT);

    final IActivity bobExpectedEditorADO =
        new EditorActivity(BOB, EditorActivity.Type.ACTIVATED, FOO_FILE_SHARED_PROJECT);

    final IActivity aliceJupiterADO =
        new JupiterActivity(
            new JupiterVectorTime(0, 0), new NoOperation(), ALICE, FOO_FILE_SHARED_PROJECT);

    final IActivity bobJupiterADO =
        new JupiterActivity(
            new JupiterVectorTime(0, 0), new NoOperation(), BOB, FOO_FILE_SHARED_PROJECT);

    activityQueuer = new ActivityQueuer();
    activityQueuer.enableQueuing(SHARED_PROJECT);

    activityQueuer.process(Arrays.asList(aliceJupiterADO, bobJupiterADO));
    activityQueuer.disableQueuing(SHARED_PROJECT);

    activities = activityQueuer.process(flush);

    assertEquals("editor ADO was not added two times", 4, activities.size());

    assertEquals("wrong (Editor)ADO was inserted", aliceExpectedEditorADO, activities.get(0));

    assertEquals("wrong (Editor)ADO was inserted", bobExpectedEditorADO, activities.get(2));
  }

  private List<IActivity> createSomeActivities() {
    IActivity startFollowingActivity = new StartFollowingActivity(ALICE, BOB);

    IActivity jupiterActivity = createJupiterActivity(FOO_FILE_SHARED_PROJECT);

    List<IActivity> activities = new ArrayList<IActivity>();
    activities.add(startFollowingActivity);
    activities.add(jupiterActivity);

    return activities;
  }

  private JupiterActivity createJupiterActivity(IFile file) {
    return new JupiterActivity(new JupiterVectorTime(0, 0), new NoOperation(), BOB, file);
  }

  private void assertListsAreEqual(List<IActivity> expected, List<IActivity> actual) {
    assertEquals(expected.size(), actual.size());
    for (int i = 0; i < expected.size(); i++) {
      assertEquals(expected.get(i), actual.get(i));
    }
  }
}
