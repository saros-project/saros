package saros.activities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.List;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import saros.editor.text.TextPosition;
import saros.editor.text.TextSelection;
import saros.filesystem.IFile;
import saros.filesystem.IPath;
import saros.filesystem.IProject;
import saros.net.xmpp.JID;
import saros.session.User;

public class ActivityOptimizerTest {

  private final User alice = new User(new JID("alice@junit"), true, true, null);
  private final User bob = new User(new JID("bob@junit"), false, false, null);

  private IFile fooFooFile;
  private IFile fooBarFile;
  private IFile barFooFile;
  private IFile barBarFile;

  private final NOPActivity nop = new NOPActivity(alice, bob, 0);

  @Before
  public void setup() {

    IProject fooProject = EasyMock.createNiceMock(IProject.class);
    IProject barProject = EasyMock.createNiceMock(IProject.class);

    IPath fooPath = EasyMock.createNiceMock(IPath.class);
    IPath barPath = EasyMock.createNiceMock(IPath.class);

    EasyMock.expect(fooPath.isAbsolute()).andStubReturn(false);
    EasyMock.expect(barPath.isAbsolute()).andStubReturn(false);

    fooFooFile = EasyMock.createNiceMock(IFile.class);
    fooBarFile = EasyMock.createNiceMock(IFile.class);
    barFooFile = EasyMock.createNiceMock(IFile.class);
    barBarFile = EasyMock.createNiceMock(IFile.class);

    EasyMock.expect(fooFooFile.getProject()).andStubReturn(fooProject);
    EasyMock.expect(fooFooFile.getProjectRelativePath()).andStubReturn(fooPath);
    EasyMock.expect(fooProject.getFile(fooPath)).andStubReturn(fooFooFile);

    EasyMock.expect(fooBarFile.getProject()).andStubReturn(fooProject);
    EasyMock.expect(fooBarFile.getProjectRelativePath()).andStubReturn(barPath);
    EasyMock.expect(fooProject.getFile(barPath)).andStubReturn(fooBarFile);

    EasyMock.expect(barFooFile.getProject()).andStubReturn(barProject);
    EasyMock.expect(barFooFile.getProjectRelativePath()).andStubReturn(fooPath);
    EasyMock.expect(barProject.getFile(fooPath)).andStubReturn(barFooFile);

    EasyMock.expect(barBarFile.getProject()).andStubReturn(barProject);
    EasyMock.expect(barBarFile.getProjectRelativePath()).andStubReturn(barPath);
    EasyMock.expect(barProject.getFile(barPath)).andStubReturn(barBarFile);

    EasyMock.replay(
        fooProject, barProject, fooPath, barPath, fooFooFile, fooBarFile, barFooFile, barBarFile);
  }

  @Test
  public void testOptimize() {
    TextSelection selection1 = new TextSelection(new TextPosition(0, 0), new TextPosition(1, 1));
    TextSelection selection2 = new TextSelection(new TextPosition(1, 1), new TextPosition(1, 1));

    TextSelectionActivity tsChange0 = new TextSelectionActivity(alice, selection1, fooFooFile);

    TextSelectionActivity tsChange1 = new TextSelectionActivity(alice, selection2, fooFooFile);

    TextSelectionActivity tsChange2 = new TextSelectionActivity(alice, selection1, fooBarFile);

    TextSelectionActivity tsChange3 = new TextSelectionActivity(alice, selection2, fooBarFile);

    TextSelectionActivity tsChange4 = new TextSelectionActivity(alice, selection1, barFooFile);

    TextSelectionActivity tsChange5 = new TextSelectionActivity(alice, selection2, barFooFile);

    TextSelectionActivity tsChange6 = new TextSelectionActivity(alice, selection1, barBarFile);

    TextSelectionActivity tsChange7 = new TextSelectionActivity(alice, selection2, barBarFile);

    // --------------------------------------------------------------------------------

    ViewportActivity vpChange0 = new ViewportActivity(alice, 0, 1, new SPath(fooFooFile));

    ViewportActivity vpChange1 = new ViewportActivity(alice, 1, 1, new SPath(fooFooFile));

    ViewportActivity vpChange2 = new ViewportActivity(alice, 0, 1, new SPath(fooBarFile));

    ViewportActivity vpChange3 = new ViewportActivity(alice, 1, 1, new SPath(fooBarFile));

    ViewportActivity vpChange4 = new ViewportActivity(alice, 0, 1, new SPath(barFooFile));

    ViewportActivity vpChange5 = new ViewportActivity(alice, 1, 1, new SPath(barFooFile));

    ViewportActivity vpChange6 = new ViewportActivity(alice, 0, 1, new SPath(barBarFile));

    ViewportActivity vpChange7 = new ViewportActivity(alice, 1, 1, new SPath(barBarFile));

    List<IActivity> activities = new ArrayList<>();

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
