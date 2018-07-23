package de.fu_berlin.inf.dpp.session.internal;

import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.JupiterActivity;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.StartFollowingActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.JupiterVectorTime;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.NoOperation;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.session.User;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ResourceActivityQueuerTest {
  private static final User ALICE = new User(new JID("Alice"), true, true, 0, 0);
  private static final User BOB = new User(new JID("Bob"), false, false, 0, 0);

  private static IProject PROJECT;

  private static SPath FOO_PATH;
  private static SPath BAR_PATH;
  private static SPath BAZ_PATH;

  private ResourceActivityQueuer activityQueuer;

  @BeforeClass
  public static void prepare() {
    PROJECT = EasyMock.createMock(IProject.class);

    FOO_PATH = new SPath(PROJECT, EasyMock.createMock(IPath.class));
    BAR_PATH = new SPath(PROJECT, EasyMock.createMock(IPath.class));
    BAZ_PATH = new SPath(PROJECT, EasyMock.createMock(IPath.class));
  }

  @Before
  public void setUp() {
    activityQueuer = new ResourceActivityQueuer();
  }

  @Test
  public void testQueuingEnabled() {
    List<IActivity> activities = new ArrayList<>();
    activities.add(new StartFollowingActivity(ALICE, BOB));
    activities.add(createJupiterActivity(FOO_PATH));
    activities.add(createJupiterActivity(BAR_PATH));
    activities.add(createJupiterActivity(BAZ_PATH));
    activities.add(createJupiterActivity(FOO_PATH));
    activities.add(createJupiterActivity(BAR_PATH));
    activities.add(createJupiterActivity(BAZ_PATH));
    activities.add(new StartFollowingActivity(ALICE, BOB));
    activities.add(createJupiterActivity(FOO_PATH));
    activities.add(createJupiterActivity(BAR_PATH));

    StartFollowingActivity startFollowingActivity = new StartFollowingActivity(ALICE, BOB);

    List<IActivity> expectedJustNonResourceAndBaz =
        activities
            .stream()
            .filter(
                c ->
                    !(c instanceof JupiterActivity)
                        || ((JupiterActivity) c).getPath().equals(BAZ_PATH))
            .collect(Collectors.toList());

    List<IActivity> expectedFooAndFollow =
        activities
            .stream()
            .filter(
                c ->
                    (c instanceof JupiterActivity
                        && ((JupiterActivity) c).getPath().equals(FOO_PATH)))
            .collect(Collectors.toList());
    expectedFooAndFollow.add(startFollowingActivity);

    List<IActivity> expectedJustBar =
        activities
            .stream()
            .filter(
                c ->
                    (c instanceof JupiterActivity
                        && ((JupiterActivity) c).getPath().equals(BAR_PATH)))
            .collect(Collectors.toList());

    /* queue */
    activityQueuer.enableQueuing(Collections.singleton(FOO_PATH));
    activityQueuer.enableQueuing(Collections.singleton(BAR_PATH));
    List<IActivity> processedActivities = activityQueuer.process(activities);
    Assert.assertEquals(
        "queue jupiter activities foo and bar", expectedJustNonResourceAndBaz, processedActivities);

    /* dequeue */
    activityQueuer.disableQueuing(FOO_PATH);
    processedActivities = activityQueuer.process(Collections.singletonList(startFollowingActivity));
    Assert.assertEquals(
        "dequeue jupiter activities foo and return new follow activity",
        expectedFooAndFollow,
        processedActivities);

    activityQueuer.disableQueuing(BAR_PATH);
    processedActivities = activityQueuer.process(Collections.emptyList());
    Assert.assertEquals("dequeue jupiter activities bar", expectedJustBar, processedActivities);
  }

  @Test
  public void testQueuingDisabled() {
    List<IActivity> activitiesOriginal = new ArrayList<>();
    activitiesOriginal.add(new StartFollowingActivity(ALICE, BOB));
    activitiesOriginal.add(createJupiterActivity(FOO_PATH));

    List<IActivity> activities = new ArrayList<>(activitiesOriginal);
    List<IActivity> processedActivities = activityQueuer.process(activities);

    Assert.assertEquals(activitiesOriginal, processedActivities);
  }

  private JupiterActivity createJupiterActivity(SPath path) {
    return new JupiterActivity(new JupiterVectorTime(0, 0), new NoOperation(), BOB, path);
  }
}
