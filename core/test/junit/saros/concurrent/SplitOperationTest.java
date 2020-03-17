package saros.concurrent;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static saros.test.util.OperationHelper.D;
import static saros.test.util.OperationHelper.I;
import static saros.test.util.OperationHelper.NOP;
import static saros.test.util.OperationHelper.S;
import static saros.test.util.OperationHelper.T;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import saros.activities.SPath;
import saros.activities.TextEditActivity;
import saros.concurrent.jupiter.Operation;
import saros.concurrent.jupiter.internal.text.SplitOperation;
import saros.concurrent.jupiter.test.util.JupiterTestCase;
import saros.concurrent.jupiter.test.util.PathFake;
import saros.filesystem.IProject;
import saros.session.User;

/**
 * Tests the logic that combines the contained operations if possible when creating text activities
 * from split operations.
 *
 * @see SplitOperation#toTextEdit(SPath, User)
 */
public class SplitOperationTest {

  protected IProject project;

  protected SPath path;
  protected User source = JupiterTestCase.createUser("source");

  @Before
  public void setUp() {
    project = createMock(IProject.class);
    replay(project);
    path = new SPath(project, new PathFake("path"));
  }

  @Test
  public void testInsertInsert() {
    // Ins(4,"0ab") + Ins(7,"cd") -> Ins(4,"0abcd")
    Operation split1 = S(I(4, "0ab"), I(7, "cd"));
    TextEditActivity expected1 = T(source, 4, "0abcd", "", path);
    assertEquals(Collections.singletonList(expected1), split1.toTextEdit(path, source));

    // Ins(4,"0ab") + Ins(4,"cd") -> Ins(4,"cd0ab")
    Operation split2 = S(I(4, "0ab"), I(4, "cd"));
    TextEditActivity expected2 = T(source, 4, "cd0ab", "", path);
    assertEquals(Collections.singletonList(expected2), split2.toTextEdit(path, source));
  }

  @Test
  public void testDeleteDelete() {
    // Del(5,"ab") + Del(5,"cde") -> Del(5,"abcde")
    Operation split1 = S(D(5, "ab"), D(5, "cde"));
    TextEditActivity expected1 = T(source, 5, "", "abcde", path);
    assertEquals(Collections.singletonList(expected1), split1.toTextEdit(path, source));

    // Del(5,"cde") + Del(5,"ab") -> Del(5,"cdeab")
    Operation split2 = S(D(5, "cde"), D(5, "ab"));
    TextEditActivity expected2 = T(source, 5, "", "cdeab", path);
    assertEquals(Collections.singletonList(expected2), split2.toTextEdit(path, source));
  }

  @Test
  public void testInsertDelete() {
    // Ins(5,"ab") + Del(5,"abcd") -> Del(5,"cd")
    Operation split1 = S(I(5, "ab"), D(5, "abcd"));
    TextEditActivity expected1 = T(source, 5, "", "cd", path);
    assertEquals(Collections.singletonList(expected1), split1.toTextEdit(path, source));

    // Ins(5,"abcde") + Del(5,"abcd") -> Ins(5,"e")
    Operation split2 = S(I(5, "abcde"), D(5, "abcd"));
    TextEditActivity expected2 = T(source, 5, "e", "", path);
    assertEquals(Collections.singletonList(expected2), split2.toTextEdit(path, source));
  }

  @Test
  public void testDeleteInsert() {
    // Del(8,"abc") + Ins(8,"ghijk") -> Replace "abc" with "ghijk"
    Operation split1 = S(D(8, "abc"), I(8, "ghijk"));
    TextEditActivity expected1 = T(source, 8, "ghijk", "abc", path);
    assertEquals(Collections.singletonList(expected1), split1.toTextEdit(path, source));
  }

  @Test
  public void testDelSplit() {
    // Split(Del(8,"abc"), Split(NoOperation, Ins(8,"ghijk")
    Operation split = S(D(8, "abc"), S(NOP(), I(8, "ghijk")));
    TextEditActivity expected = T(source, 8, "ghijk", "abc", path);
    assertEquals(Collections.singletonList(expected), split.toTextEdit(path, source));
  }

  @Test
  public void testSplitChain1() {
    // Split(Split(A,B), Split(C,D)) with B and C can be combined
    Operation split = S(S(D(8, "uvw"), I(2, "abcde")), S(D(2, "abcd"), D(15, "xyz")));

    List<TextEditActivity> expected = new LinkedList<>();
    expected.add(T(source, 8, "", "uvw", path));
    expected.add(T(source, 2, "e", "", path));
    expected.add(T(source, 15, "", "xyz", path));

    assertEquals(expected, split.toTextEdit(path, source));
  }

  @Test
  public void testSplitChain2() {
    // Split(Split(A,B), Split(C,D)) with B, C and D can be combined
    Operation split = S(S(D(8, "uvw"), I(2, "abcde")), S(D(2, "abcd"), I(3, "xyz")));

    List<TextEditActivity> expected = new LinkedList<>();
    expected.add(T(source, 8, "", "uvw", path));
    expected.add(T(source, 2, "exyz", "", path));

    assertEquals(expected, split.toTextEdit(path, source));
  }

  @Test
  public void testSplitChain3() {
    // Split(Split(A,B), Split(C,D)) with A, B, C and D can be combined
    Operation split = S(S(D(8, "abc"), D(8, "defg")), S(I(8, "1234"), I(12, "56")));

    List<TextEditActivity> expected = new LinkedList<>();
    expected.add(T(source, 8, "123456", "abcdefg", path));

    assertEquals(expected, split.toTextEdit(path, source));
  }
}
