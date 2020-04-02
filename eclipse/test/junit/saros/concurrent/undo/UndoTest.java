package saros.concurrent.undo;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static saros.test.util.OperationHelper.D;
import static saros.test.util.OperationHelper.I;
import static saros.test.util.OperationHelper.S;

import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Test;
import saros.activities.SPath;
import saros.concurrent.jupiter.Operation;
import saros.concurrent.jupiter.internal.text.NoOperation;
import saros.concurrent.undo.OperationHistory.Type;
import saros.filesystem.IProject;
import saros.filesystem.ResourceAdapterFactory;

/** testing TextOperationHistory and UndoManager */
public class UndoTest {

  // private static final Logger log = Logger.getLogger(UndoTest.class);

  protected IProject project;

  protected SPath path1;
  protected SPath path2;

  protected OperationHistory history;
  protected UndoManager undoManager;

  @Before
  public void setUp() {
    project = createMock(IProject.class);
    replay(project);
    undoManager = new UndoManager();
    history = undoManager.getHistory();
    path1 = new SPath(project, ResourceAdapterFactory.create(new Path("path1")));
    path2 = new SPath(project, ResourceAdapterFactory.create(new Path("path2")));
  }

  protected Operation nop() {
    return new NoOperation();
  }

  protected Operation undo(SPath path) {
    return undoManager.calcUndoOperation(path);
  }

  protected Operation redo(SPath path) {
    return undoManager.calcRedoOperation(path);
  }

  @Test
  public void testEmptyHistory() {
    assertEquals(nop(), undo(path1));
    assertEquals(nop(), undo(path2));
    assertEquals(nop(), redo(path1));
  }

  @Test
  public void testHistoryWithoutLocals() {
    history.add(path1, Type.REMOTE, I(5, "abc"));
    history.add(path1, Type.REMOTE, D(11, "ghi"));

    assertEquals(nop(), undo(path1));
    assertEquals(nop(), undo(path2));
    assertEquals(nop(), redo(path1));
  }

  @Test
  public void testHistoryWithoutRemotes() {
    history.add(path1, Type.LOCAL, I(5, "abc"));
    history.add(path1, Type.LOCAL, D(11, "ghi"));

    Operation expected = I(11, "ghi");

    assertEquals(nop(), redo(path1));
    assertEquals(expected, undo(path1));
    assertEquals(expected.invert(), redo(path1));

    assertEquals(nop(), undo(path2));
  }

  @Test
  public void testMixedHistoryWithOnePath() {
    // Text: 0123456789
    history.add(path1, Type.LOCAL, I(8, "first")); // 01234567first89
    history.add(path1, Type.REMOTE, I(2, "XXX")); // 01XXX234567first89
    history.add(path1, Type.LOCAL, I(5, "sec")); // 01XXXsec234567first89
    history.add(path1, Type.REMOTE, D(8, "234567")); // 01XXXsecfirst89
    history.add(path1, Type.REMOTE, D(1, "1X")); // 0XXsecfirst89

    Operation expected = D(3, "sec");

    assertEquals(expected, undo(path1));
    assertEquals(expected.invert(), redo(path1));
    assertEquals(nop(), undo(path2));
  }

  @Test
  public void testMixedHistoryWithTwoPaths() {
    // Text: 0123456789
    history.add(path1, Type.LOCAL, I(8, "first")); // 01234567first89
    history.add(path1, Type.REMOTE, I(2, "XXX")); // 01XXX234567first89
    history.add(path1, Type.LOCAL, I(5, "sec")); // 01XXXsec234567first89
    history.add(path1, Type.REMOTE, D(8, "234567")); // 01XXXsecfirst89
    history.add(path1, Type.REMOTE, D(1, "1X")); // 0XXsecfirst89

    Operation expected1 = D(3, "sec");

    // Text in path2: 123456
    history.add(path2, Type.REMOTE, D(0, "123")); // 456
    history.add(path2, Type.LOCAL, I(0, "ace")); // ace456
    history.add(path2, Type.REMOTE, I(1, "b")); // abce456
    history.add(path2, Type.REMOTE, I(3, "d")); // abcde456
    history.add(path2, Type.REMOTE, D(5, "456")); // abcde

    Operation expected2 = S(D(0, "a"), S(D(1, "c"), D(2, "e")));

    assertEquals(nop(), redo(path1));
    assertEquals(expected1, undo(path1));
    assertEquals(expected1.invert(), redo(path1));

    assertEquals(nop(), redo(path2));
    assertEquals(expected2, undo(path2));
    assertEquals(expected2.invert(), redo(path2));
  }

  @Test
  public void testTwoUndos() {
    // Text: 0123456789
    history.add(path1, Type.LOCAL, I(8, "first")); // 01234567first89
    history.add(path1, Type.REMOTE, I(2, "XXX")); // 01XXX234567first89
    history.add(path1, Type.LOCAL, I(5, "sec")); // 01XXXsec234567first89
    history.add(path1, Type.REMOTE, D(8, "234567")); // 01XXXsecfirst89
    history.add(path1, Type.REMOTE, D(1, "1XX")); // 0Xsecfirst89

    Operation expected1 = D(2, "sec");
    // first undo -> 0Xfirst89
    Operation expected2 = D(2, "first");
    // second undo -> 0X89

    assertEquals(expected1, undo(path1)); // 0Xfirst89
    assertEquals(expected2, undo(path1)); // 0X89
    assertEquals(expected2.invert(), redo(path1)); // 0Xfirst89
    assertEquals(expected1.invert(), redo(path1)); // 0Xsecfirst89
  }

  @Test
  public void testRedoAfterNewRemotes() {
    // Text: 0123456789
    history.add(path1, Type.LOCAL, I(8, "first")); // 01234567first89
    history.add(path1, Type.REMOTE, I(2, "XXX")); // 01XXX234567first89
    history.add(path1, Type.LOCAL, I(5, "sec")); // 01XXXsec234567first89
    history.add(path1, Type.REMOTE, D(8, "234567")); // 01XXXsecfirst89
    history.add(path1, Type.REMOTE, D(1, "1X")); // 0XXsecfirst89

    Operation expected = D(3, "sec");
    // first undo -> 0XXfirst89
    assertEquals(expected, undo(path1));

    history.add(path1, Type.REMOTE, I(1, "123")); // 0123XXfirst89

    expected = D(6, "first");
    // second undo -> 0123XX89
    assertEquals(expected, undo(path1));

    history.add(path1, Type.REMOTE, D(4, "XX")); // 012389

    expected = I(4, "first", 6);
    // first redo (redoes second undo) -> 0123first89
    assertEquals(expected, redo(path1));

    history.add(path1, Type.REMOTE, D(0, "0123")); // first89

    expected = I(0, "sec", 3);
    // second redo -> secfirst89

    assertEquals(expected, redo(path1));
  }

  @Test
  public void testUndoRedoUndo() {
    history.add(path1, Type.LOCAL, I(3, "abc"));
    history.add(path1, Type.LOCAL, I(6, "defghi"));

    undo(path1);
    redo(path1);

    Operation expected = D(6, "defghi");
    assertEquals(expected, undo(path1));
  }

  @Test
  public void testUndoUndoRedoRedoUndo() {
    history.add(path1, Type.LOCAL, I(3, "abc"));
    history.add(path1, Type.LOCAL, I(6, "defghi")); // abcdefghi
    history.add(path1, Type.LOCAL, D(5, "cde")); // abfghi

    undo(path1); // abcdefghi
    undo(path1); // abc
    redo(path1); // abcdefghi

    Operation expected = D(5, "cde");
    assertEquals(expected, redo(path1));

    expected = I(5, "cde");
    assertEquals(expected, undo(path1));
  }

  /**
   * This test currently fails, because the UndoManager is using GOTOTransform which swallows a
   * delete during undo.
   *
   * <p>Reworking the UndoManager is necessary.
   *
   * <p>Ask Sebastian
   */
  @Test
  public void testBackspaceUndo() {
    history.add(path1, Type.LOCAL, I(3, "abc"));
    history.add(path1, Type.LOCAL, D(3, "abc"));

    Operation expected = I(3, "abc");
    assertEquals(expected, undo(path1));

    // FIXME This test always fails
    // expected = D(3, "abc");
    // assertEquals(expected, undo(path1));
  }
}
