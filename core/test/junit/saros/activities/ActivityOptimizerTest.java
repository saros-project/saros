package saros.activities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.List;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import saros.filesystem.IPath;
import saros.filesystem.IProject;
import saros.net.xmpp.JID;
import saros.session.User;

public class ActivityOptimizerTest {

  private final User alice = new User(new JID("alice@junit"), true, true, 0, 0);
  private final User bob = new User(new JID("bob@junit"), false, false, 0, 0);

  private IPath fooPath;
  private IPath barPath;

  private IProject fooProject;
  private IProject barProject;

  private final NOPActivity nop = new NOPActivity(alice, bob, 0);

  @Before
  public void setup() {

    fooProject = EasyMock.createNiceMock(IProject.class);
    barProject = EasyMock.createNiceMock(IProject.class);

    fooPath = EasyMock.createNiceMock(IPath.class);
    barPath = EasyMock.createNiceMock(IPath.class);

    EasyMock.replay(fooProject, barProject, fooPath, barPath);
  }

  @Test
  public void testOptimize() {

    SPath foofooSPath = new SPath(fooProject, fooPath);

    SPath foobarSPath = new SPath(fooProject, barPath);

    SPath barfooSPath = new SPath(barProject, fooPath);

    SPath barbarSPath = new SPath(barProject, barPath);

    TextSelectionActivity tsChange0 = new TextSelectionActivity(alice, 0, 1, foofooSPath);

    TextSelectionActivity tsChange1 = new TextSelectionActivity(alice, 1, 1, foofooSPath);

    TextSelectionActivity tsChange2 = new TextSelectionActivity(alice, 0, 1, foobarSPath);

    TextSelectionActivity tsChange3 = new TextSelectionActivity(alice, 1, 1, foobarSPath);

    TextSelectionActivity tsChange4 = new TextSelectionActivity(alice, 0, 1, barfooSPath);

    TextSelectionActivity tsChange5 = new TextSelectionActivity(alice, 1, 1, barfooSPath);

    TextSelectionActivity tsChange6 = new TextSelectionActivity(alice, 0, 1, barbarSPath);

    TextSelectionActivity tsChange7 = new TextSelectionActivity(alice, 1, 1, barbarSPath);

    // --------------------------------------------------------------------------------

    ViewportActivity vpChange0 = new ViewportActivity(alice, 0, 1, foofooSPath);

    ViewportActivity vpChange1 = new ViewportActivity(alice, 1, 1, foofooSPath);

    ViewportActivity vpChange2 = new ViewportActivity(alice, 0, 1, foobarSPath);

    ViewportActivity vpChange3 = new ViewportActivity(alice, 1, 1, foobarSPath);

    ViewportActivity vpChange4 = new ViewportActivity(alice, 0, 1, barfooSPath);

    ViewportActivity vpChange5 = new ViewportActivity(alice, 1, 1, barfooSPath);

    ViewportActivity vpChange6 = new ViewportActivity(alice, 0, 1, barbarSPath);

    ViewportActivity vpChange7 = new ViewportActivity(alice, 1, 1, barbarSPath);

    List<IActivity> activities = new ArrayList<IActivity>();

    activities.add(tsChange0);
    activities.add(nop);
    activities.add(tsChange1);
    activities.add(nop);
    activities.add(tsChange2);
    activities.add(nop);
    activities.add(tsChange3);
    activities.add(nop);
    activities.add(tsChange4);
    activities.add(nop);
    activities.add(tsChange5);
    activities.add(nop);
    activities.add(tsChange6);
    activities.add(nop);
    activities.add(tsChange7);
    activities.add(nop);
    activities.add(vpChange0);
    activities.add(nop);
    activities.add(vpChange1);
    activities.add(nop);
    activities.add(vpChange2);
    activities.add(nop);
    activities.add(vpChange3);
    activities.add(nop);
    activities.add(vpChange4);
    activities.add(nop);
    activities.add(vpChange5);
    activities.add(nop);
    activities.add(vpChange6);
    activities.add(nop);
    activities.add(vpChange7);
    activities.add(nop);

    List<IActivity> optimized = ActivityOptimizer.optimize(activities);

    assertEquals(
        "activities are not optimally optimized", /* NOP */
        16 + /* TS */ 4 + /* VP */ 4,
        optimized.size());

    assertRange(0, 0, optimized, nop);
    assertRange(1, 1, optimized, tsChange1);
    assertRange(2, 3, optimized, nop);
    assertRange(4, 4, optimized, tsChange3);
    assertRange(5, 6, optimized, nop);
    assertRange(7, 7, optimized, tsChange5);
    assertRange(8, 9, optimized, nop);
    assertRange(10, 10, optimized, tsChange7);
    assertRange(11, 12, optimized, nop);
    assertRange(13, 13, optimized, vpChange1);
    assertRange(14, 15, optimized, nop);
    assertRange(16, 16, optimized, vpChange3);
    assertRange(17, 18, optimized, nop);
    assertRange(19, 19, optimized, vpChange5);
    assertRange(20, 21, optimized, nop);
    assertRange(22, 22, optimized, vpChange7);
    assertRange(23, 23, optimized, nop);
  }

  private void assertRange(int l, int h, List<IActivity> activities, IActivity activity) {
    for (int i = l; i <= h; i++)
      assertSame("optimization resulted in wrong activity order", activity, activities.get(i));
  }
}
