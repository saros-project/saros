package de.fu_berlin.inf.dpp.editor.remote;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import de.fu_berlin.inf.dpp.activities.EditorActivity;
import de.fu_berlin.inf.dpp.activities.EditorActivity.Type;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.TextSelectionActivity;
import de.fu_berlin.inf.dpp.activities.ViewportActivity;
import de.fu_berlin.inf.dpp.editor.text.LineRange;
import de.fu_berlin.inf.dpp.editor.text.TextSelection;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IPathFactory;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.session.User;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class UserEditorStateTest {

  /** Unit under test */
  private UserEditorState state;

  private static SPath pathA;
  private static SPath pathB;

  private static User source;

  @BeforeClass
  public static void prepare() {
    /* Mocks */
    IPathFactory pathFactory = EasyMock.createMock(IPathFactory.class);
    IPath pathMockA = mockPath("/foo/src/Main.java", pathFactory);
    IPath pathMockB = mockPath("/foo/src/Second.java", pathFactory);

    IProject project = EasyMock.createNiceMock(IProject.class);

    EasyMock.replay(pathFactory, project);

    pathA = new SPath(project, pathMockA);
    pathB = new SPath(project, pathMockB);

    /** */
    source = EasyMock.createMock(User.class);
    EasyMock.replay(source);
  }

  private static IPath mockPath(String filename, IPathFactory pathFactory) {
    IPath mock = EasyMock.createMock(IPath.class);
    expect(mock.isAbsolute()).andStubReturn(false);
    expect(mock.toPortableString()).andStubReturn(filename);

    expect(pathFactory.fromPath(mock)).andStubReturn(filename);
    expect(pathFactory.fromString(filename)).andStubReturn(mock);

    EasyMock.replay(mock);

    return mock;
  }

  @Before
  public void setup() {
    state = new UserEditorState();
  }

  @Test
  public void testFreshUserState() {
    assertNotNull("fresh user state should have an empty list of editors", state.getOpenEditors());
    assertNull("fresh user state should have no active editor", state.getActiveEditorState());
  }

  @Test
  public void testIgnoreActivitiesOnActivatedEditors() {
    assertFalse(
        "user state should not exist before activity reception",
        state.getOpenEditors().contains(pathA));

    select(pathA, 5, 15);
    view(pathA, 10, 30);

    assertFalse(
        "user state should not exist after awareness information " + "on non-activated editor",
        state.getOpenEditors().contains(pathA));

    activate(pathA);

    assertTrue(
        "user state should exist after editor was opened", state.getOpenEditors().contains(pathA));
  }

  @Test
  public void testUpdateSelections() {
    activate(pathA);
    select(pathA, 5, 15);

    TextSelection s = state.getEditorState(pathA).getSelection();

    assertEquals("selection offset should be set", 5, s.getOffset());
    assertEquals("selection length should be set", 15, s.getLength());

    select(pathA, 8, 38);

    s = state.getEditorState(pathA).getSelection();

    assertEquals("selection offset should be updated", 8, s.getOffset());
    assertEquals("selection length should be updated", 38, s.getLength());
  }

  @Test
  public void testUpdateViewports() {
    activate(pathA);
    view(pathA, 5, 50);

    LineRange v = state.getEditorState(pathA).getViewport();

    assertEquals("viewport offset should be set", 5, v.getStartLine());
    assertEquals("viewport length should be set", 50, v.getNumberOfLines());

    view(pathA, 10, 45);

    v = state.getEditorState(pathA).getViewport();

    assertEquals("viewport offset should be updated", 10, v.getStartLine());
    assertEquals("viewport length should be updated", 45, v.getNumberOfLines());
  }

  @Test
  public void testActivateAndClose() {
    activate(pathA);
    activate(pathB);

    assertEquals("both activated editors should be open", 2, state.getOpenEditors().size());

    assertEquals(
        "last activated editor should be active", pathB, state.getActiveEditorState().getPath());

    activate(pathA);

    assertEquals(
        "last activated editor should be active", pathA, state.getActiveEditorState().getPath());

    close(pathA);

    assertNull(
        "no editor should be active after the active editor " + "was closed",
        state.getActiveEditorState());
    assertEquals("only one editor should be open", 1, state.getOpenEditors().size());
  }

  @Test
  public void testMultiplePaths() {
    activate(pathA);
    activate(pathB);

    select(pathA, 1, 2);
    view(pathA, 3, 4);

    select(pathB, 5, 6);
    view(pathB, 7, 8);

    EditorState stateA = state.getEditorState(pathA);
    TextSelection selectionA = stateA.getSelection();

    assertEquals(1, selectionA.getOffset());
    assertEquals(2, selectionA.getLength());

    LineRange viewportA = stateA.getViewport();

    assertEquals(3, viewportA.getStartLine());
    assertEquals(4, viewportA.getNumberOfLines());

    EditorState stateB = state.getEditorState(pathB);
    TextSelection selectionB = stateB.getSelection();

    assertEquals(5, selectionB.getOffset());
    assertEquals(6, selectionB.getLength());

    LineRange viewportB = stateB.getViewport();

    assertEquals(7, viewportB.getStartLine());
    assertEquals(8, viewportB.getNumberOfLines());
  }

  private void close(SPath path) {
    state.consumer.exec(new EditorActivity(source, Type.CLOSED, path));
  }

  private void activate(SPath path) {
    state.consumer.exec(new EditorActivity(source, Type.ACTIVATED, path));
  }

  private void view(SPath path, int start, int length) {
    state.consumer.exec(new ViewportActivity(source, start, length, path));
  }

  private void select(SPath path, int offset, int length) {
    state.consumer.exec(new TextSelectionActivity(source, offset, length, path));
  }
}
