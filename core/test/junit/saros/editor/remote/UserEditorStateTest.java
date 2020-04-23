package saros.editor.remote;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import saros.activities.EditorActivity;
import saros.activities.EditorActivity.Type;
import saros.activities.TextSelectionActivity;
import saros.activities.ViewportActivity;
import saros.editor.text.LineRange;
import saros.editor.text.TextPosition;
import saros.editor.text.TextSelection;
import saros.filesystem.IFile;
import saros.session.User;

public class UserEditorStateTest {

  /** Unit under test */
  private UserEditorState state;

  private static IFile fileA;
  private static IFile fileB;

  private static User source;

  @BeforeClass
  public static void prepare() {
    /* Mocks */
    fileA = EasyMock.createNiceMock(IFile.class);
    fileB = EasyMock.createNiceMock(IFile.class);

    source = EasyMock.createMock(User.class);
    EasyMock.replay(source);
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
        state.getOpenEditors().contains(fileA));

    // Selection values were chosen at "random"
    TextSelection selection = new TextSelection(new TextPosition(5, 15), new TextPosition(20, 1));
    select(fileA, selection);
    view(fileA, 10, 30);

    assertFalse(
        "user state should not exist after awareness information " + "on non-activated editor",
        state.getOpenEditors().contains(fileA));

    activate(fileA);

    assertTrue(
        "user state should exist after editor was opened", state.getOpenEditors().contains(fileA));
  }

  @Test
  public void testUpdateSelections() {
    activate(fileA);

    TextSelection selection1 = new TextSelection(new TextPosition(5, 3), new TextPosition(8, 15));
    select(fileA, selection1);

    TextSelection stateSelection = state.getEditorState(fileA).getSelection();

    assertEquals("selection should be set", selection1, stateSelection);

    TextSelection selection2 = new TextSelection(new TextPosition(0, 7), new TextPosition(34, 15));
    select(fileA, selection2);

    stateSelection = state.getEditorState(fileA).getSelection();

    assertEquals("selection should be updated", selection2, stateSelection);
  }

  @Test
  public void testUpdateViewports() {
    activate(fileA);
    view(fileA, 5, 50);

    LineRange v = state.getEditorState(fileA).getViewport();

    assertEquals("viewport offset should be set", 5, v.getStartLine());
    assertEquals("viewport length should be set", 50, v.getNumberOfLines());

    view(fileA, 10, 45);

    v = state.getEditorState(fileA).getViewport();

    assertEquals("viewport offset should be updated", 10, v.getStartLine());
    assertEquals("viewport length should be updated", 45, v.getNumberOfLines());
  }

  @Test
  public void testActivateAndClose() {
    activate(fileA);
    activate(fileB);

    assertEquals("both activated editors should be open", 2, state.getOpenEditors().size());

    assertEquals(
        "last activated editor should be active", fileB, state.getActiveEditorState().getFile());

    activate(fileA);

    assertEquals(
        "last activated editor should be active", fileA, state.getActiveEditorState().getFile());

    close(fileA);

    assertNull(
        "no editor should be active after the active editor " + "was closed",
        state.getActiveEditorState());
    assertEquals("only one editor should be open", 1, state.getOpenEditors().size());
  }

  @Test
  public void testMultipleFiles() {
    activate(fileA);
    activate(fileB);

    // Selection values were chosen at "random"
    TextSelection selectionA = new TextSelection(new TextPosition(1, 0), new TextPosition(2, 3));
    select(fileA, selectionA);
    view(fileA, 3, 4);

    TextSelection selectionB = new TextSelection(new TextPosition(5, 4), new TextPosition(6, 1));
    select(fileB, selectionB);
    view(fileB, 7, 8);

    EditorState stateA = state.getEditorState(fileA);
    TextSelection selectionStateA = stateA.getSelection();

    assertEquals("selection should be set for first resource", selectionA, selectionStateA);

    LineRange viewportA = stateA.getViewport();

    assertEquals("viewport should be set for first resource", 3, viewportA.getStartLine());
    assertEquals("viewport should be set for first resource", 4, viewportA.getNumberOfLines());

    EditorState stateB = state.getEditorState(fileB);
    TextSelection selectionStateB = stateB.getSelection();

    assertEquals("selection should be set for second resource", selectionB, selectionStateB);

    LineRange viewportB = stateB.getViewport();

    assertEquals("viewport should be set for second resource", 7, viewportB.getStartLine());
    assertEquals("viewport should be set for second resource", 8, viewportB.getNumberOfLines());
  }

  private void close(IFile file) {
    state.consumer.exec(new EditorActivity(source, Type.CLOSED, file));
  }

  private void activate(IFile file) {
    state.consumer.exec(new EditorActivity(source, Type.ACTIVATED, file));
  }

  private void view(IFile file, int start, int length) {
    state.consumer.exec(new ViewportActivity(source, start, length, file));
  }

  private void select(IFile file, TextSelection selection) {
    state.consumer.exec(new TextSelectionActivity(source, selection, file));
  }
}
