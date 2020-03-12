package saros.concurrent;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static saros.test.util.OperationHelper.D;
import static saros.test.util.OperationHelper.EOL;
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
    // Ins((0,4),"0ab") + Ins((0,7),"cd") -> Ins((0,4),"0abcd")
    Operation split1 = S(I(4, "0ab"), I(7, "cd"));
    TextEditActivity expected1 = T(source, 4, "0abcd", "", path);
    assertEquals(Collections.singletonList(expected1), split1.toTextEdit(path, source));

    // Ins((0,4),"0ab") + Ins((0,4),"cd") -> Ins((0,4),"cd0ab")
    Operation split2 = S(I(4, "0ab"), I(4, "cd"));
    TextEditActivity expected2 = T(source, 4, "cd0ab", "", path);
    assertEquals(Collections.singletonList(expected2), split2.toTextEdit(path, source));
  }

  @Test
  public void testMultiLineInsertInsertCase1() {
    // Ins((0,4),"0ab\n") + Ins((1,0),"cd") -> Ins((0,4),"0ab\ncd")
    Operation split1 = S(I(4, "0ab" + EOL), I(1, 0, "cd"));
    TextEditActivity expected1 = T(source, 4, "0ab" + EOL + "cd", "", path);
    assertEquals(Collections.singletonList(expected1), split1.toTextEdit(path, source));

    // Ins((1,4),"0ab\ncd") + Ins((2,2),"efg") -> Ins((1,4),"0ab\ncdefg")
    Operation split2 = S(I(1, 4, "0ab" + EOL + "cd"), I(2, 2, "efg"));
    TextEditActivity expected2 = T(source, 1, 4, "0ab" + EOL + "cdefg", "", path);
    assertEquals(Collections.singletonList(expected2), split2.toTextEdit(path, source));

    // Ins((3,4),"0ab") + Ins((3,7),"cd\n") -> Ins((3,4),"0abcd\n")
    Operation split3 = S(I(3, 4, "0ab"), I(3, 7, "cd" + EOL));
    TextEditActivity expected3 = T(source, 3, 4, "0abcd" + EOL, "", path);
    assertEquals(Collections.singletonList(expected3), split3.toTextEdit(path, source));

    // Ins((0,4),"0ab") + Ins((0,7),"cd\nef") -> Ins((0,4),"0abcd\nef")
    Operation split4 = S(I(4, "0ab"), I(7, "cd" + EOL + "ef"));
    TextEditActivity expected4 = T(source, 4, "0abcd" + EOL + "ef", "", path);
    assertEquals(Collections.singletonList(expected4), split4.toTextEdit(path, source));

    // Ins((0,4),"0ab\ncd") + Ins((1,2),"ef\ngh") -> Ins((0,4),"0ab\ncdef\ngh")
    Operation split5 = S(I(4, "0ab" + EOL + "cd"), I(1, 2, "ef" + EOL + "gh"));
    TextEditActivity expected5 = T(source, 4, "0ab" + EOL + "cdef" + EOL + "gh", "", path);
    assertEquals(Collections.singletonList(expected5), split5.toTextEdit(path, source));

    // Ins((9,4),"0ab\ncd\nef\nghx") + Ins((12,3),"ij\nkl\nmn\nop") ->
    // Ins((9,4),"0ab\ncd\nef\nghxij\nkl\mn\nop")
    Operation split6 =
        S(
            I(9, 4, "0ab" + EOL + "cd" + EOL + "ef" + EOL + "ghx"),
            I(12, 3, "ij" + EOL + "kl" + EOL + "mn" + EOL + "op"));
    TextEditActivity expected6 =
        T(
            source,
            9,
            4,
            "0ab" + EOL + "cd" + EOL + "ef" + EOL + "ghxij" + EOL + "kl" + EOL + "mn" + EOL + "op",
            "",
            path);
    assertEquals(Collections.singletonList(expected6), split6.toTextEdit(path, source));
  }

  @Test
  public void testMultiLineInsertInsertCase2() {
    // Ins((3,4),"0ab\n") + Ins((3,4),"cd") -> Ins((3,4),"cd0ab\n")
    Operation split1 = S(I(3, 4, "0ab" + EOL), I(3, 4, "cd"));
    TextEditActivity expected1 = T(source, 3, 4, "cd0ab" + EOL, "", path);
    assertEquals(Collections.singletonList(expected1), split1.toTextEdit(path, source));

    // Ins((5,4),"0ab\ncd") + Ins((5,4),"efg") -> Ins((5,4),"efg0ab\ncd")
    Operation split2 = S(I(5, 4, "0ab" + EOL + "cd"), I(5, 4, "efg"));
    TextEditActivity expected2 = T(source, 5, 4, "efg0ab" + EOL + "cd", "", path);
    assertEquals(Collections.singletonList(expected2), split2.toTextEdit(path, source));

    // Ins((0,4),"0ab") + Ins((0,7),"cd\n") -> Ins((0,4),"0abcd\n")
    Operation split3 = S(I(4, "0ab"), I(4, "cd" + EOL));
    TextEditActivity expected3 = T(source, 4, "cd" + EOL + "0ab", "", path);
    assertEquals(Collections.singletonList(expected3), split3.toTextEdit(path, source));

    // Ins((0,4),"0ab") + Ins((0,4),"cd\nef") -> Ins((0,4),"cd\nef0ab")
    Operation split4 = S(I(4, "0ab"), I(4, "cd" + EOL + "ef"));
    TextEditActivity expected4 = T(source, 4, "cd" + EOL + "ef0ab", "", path);
    assertEquals(Collections.singletonList(expected4), split4.toTextEdit(path, source));

    // Ins((1,4),"0ab\ncd") + Ins((1,4),"ef\ngh") -> Ins((1,4),"ef\ngh0ab\ncd")
    Operation split5 = S(I(1, 4, "0ab" + EOL + "cd"), I(1, 4, "ef" + EOL + "gh"));
    TextEditActivity expected5 = T(source, 1, 4, "ef" + EOL + "gh0ab" + EOL + "cd", "", path);
    assertEquals(Collections.singletonList(expected5), split5.toTextEdit(path, source));

    // Ins((0,4),"0ab\ncd\nef\ngh") + Ins((0,4),"ij\nkl\nmn\nop") ->
    // Ins((0,4),"ij\nkl\nmn\nop0ab\ncd\nef\ngh")
    Operation split6 =
        S(
            I(4, "0ab" + EOL + "cd" + EOL + "ef" + EOL + "gh"),
            I(4, "ij" + EOL + "kl" + EOL + "mn" + EOL + "op"));
    TextEditActivity expected6 =
        T(
            source,
            4,
            "ij" + EOL + "kl" + EOL + "mn" + EOL + "op0ab" + EOL + "cd" + EOL + "ef" + EOL + "gh",
            "",
            path);
    assertEquals(Collections.singletonList(expected6), split6.toTextEdit(path, source));
  }

  @Test
  public void testDeleteDelete() {
    // Del((0,5),"ab") + Del((0,5),"cde") -> Del((0,5),"abcde")
    Operation split1 = S(D(5, "ab"), D(5, "cde"));
    TextEditActivity expected1 = T(source, 5, "", "abcde", path);
    assertEquals(Collections.singletonList(expected1), split1.toTextEdit(path, source));

    // Del((0,5),"cde") + Del((0,5),"ab") -> Del((0,5),"cdeab")
    Operation split2 = S(D(5, "cde"), D(5, "ab"));
    TextEditActivity expected2 = T(source, 5, "", "cdeab", path);
    assertEquals(Collections.singletonList(expected2), split2.toTextEdit(path, source));

    // Del((0,7),"cde") + Del((0,5),"ab") -> Del((0,5),"abcde")
    Operation split3 = S(D(7, "cde"), D(5, "ab"));
    TextEditActivity expected3 = T(source, 5, "", "abcde", path);
    assertEquals(Collections.singletonList(expected3), split3.toTextEdit(path, source));
  }

  @Test
  public void testMultiLineDeleteDeleteCase1() {
    // Del((0,5),"ab\n") + Del((0,5),"cde") -> Del((0,5),"ab\ncde")
    Operation split1 = S(D(5, "ab" + EOL), D(5, "cde"));
    TextEditActivity expected1 = T(source, 5, "", "ab" + EOL + "cde", path);
    assertEquals(Collections.singletonList(expected1), split1.toTextEdit(path, source));

    // Del((2,5),"ab\ncde") + Del((2,5),"fg") -> Del((2,5),"ab\ncdefg")
    Operation split2 = S(D(2, 5, "ab" + EOL + "cde"), D(2, 5, "fg"));
    TextEditActivity expected2 = T(source, 2, 5, "", "ab" + EOL + "cdefg", path);
    assertEquals(Collections.singletonList(expected2), split2.toTextEdit(path, source));

    // Del((15,5),"ab") + Del((15,5),"cde\n") -> Del((15,5),"abcde\n")
    Operation split3 = S(D(15, 5, "ab"), D(15, 5, "cde" + EOL));
    TextEditActivity expected3 = T(source, 15, 5, "", "abcde" + EOL, path);
    assertEquals(Collections.singletonList(expected3), split3.toTextEdit(path, source));

    // Del((0,5),"ab") + Del((0,5),"cde\nfg") -> Del((0,5),"abcde\nfg")
    Operation split4 = S(D(5, "ab"), D(5, "cde" + EOL + "fg"));
    TextEditActivity expected4 = T(source, 5, "", "abcde" + EOL + "fg", path);
    assertEquals(Collections.singletonList(expected4), split4.toTextEdit(path, source));

    // Del((3,5),"ab\ncde") + Del((3,5),"fg\nhi") -> Del((3,5),"ab\ncdefg\nhi")
    Operation split5 = S(D(3, 5, "ab" + EOL + "cde"), D(3, 5, "fg" + EOL + "hi"));
    TextEditActivity expected5 = T(source, 3, 5, "", "ab" + EOL + "cdefg" + EOL + "hi", path);
    assertEquals(Collections.singletonList(expected5), split5.toTextEdit(path, source));

    // Del((52,5),"0ab\ncd\nef\ngh") + Del((52,5),"ij\nkl\nmn\nop") ->
    // Del((52,4),"0ab\ncd\nef\nghij\nkl\nmn\nop")
    Operation split6 =
        S(
            D(52, 5, "0ab" + EOL + "cd" + EOL + "ef" + EOL + "gh"),
            D(52, 5, "ij" + EOL + "kl" + EOL + "mn" + EOL + "op"));
    TextEditActivity expected6 =
        T(
            source,
            52,
            5,
            "",
            "0ab" + EOL + "cd" + EOL + "ef" + EOL + "gh" + "ij" + EOL + "kl" + EOL + "mn" + EOL
                + "op",
            path);
    assertEquals(Collections.singletonList(expected6), split6.toTextEdit(path, source));
  }

  @Test
  public void testMultiLineDeleteDeleteCase2() {
    // Del((0,8),"ab\n") + Del((0,5),"cde") -> Del((0,5),"cdeab\n")
    Operation split1 = S(D(8, "ab" + EOL), D(5, "cde"));
    TextEditActivity expected1 = T(source, 5, "", "cdeab" + EOL, path);
    assertEquals(Collections.singletonList(expected1), split1.toTextEdit(path, source));

    // Del((1,7),"ab\ncde") + Del((1,5),"fg") -> Del((1,5),"fgab\ncde")
    Operation split2 = S(D(1, 7, "ab" + EOL + "cde"), D(1, 5, "fg"));
    TextEditActivity expected2 = T(source, 1, 5, "", "fgab" + EOL + "cde", path);
    assertEquals(Collections.singletonList(expected2), split2.toTextEdit(path, source));

    // Del((12,0),"ab") + Del((11,3),"cde\n") -> Del((11,5),"cde\nab")
    Operation split3 = S(D(12, 0, "ab"), D(11, 3, "cde" + EOL));
    TextEditActivity expected3 = T(source, 11, 3, "", "cde" + EOL + "ab", path);
    assertEquals(Collections.singletonList(expected3), split3.toTextEdit(path, source));

    // Del((1,5),"ab") + Del((0,5),"cde\nfghij") -> Del((0,5),"cde\nfghijab")
    Operation split4 = S(D(1, 5, "ab"), D(5, "cde" + EOL + "fghij"));
    TextEditActivity expected4 = T(source, 5, "", "cde" + EOL + "fghijab", path);
    assertEquals(Collections.singletonList(expected4), split4.toTextEdit(path, source));

    // Del((8,2),"ab\ncde") + Del((7,2),"fg\nhi") -> Del((7,2),"fg\nhiab\ncde")
    Operation split5 = S(D(8, 2, "ab" + EOL + "cde"), D(7, 2, "fg" + EOL + "hi"));
    TextEditActivity expected5 = T(source, 7, 2, "", "fg" + EOL + "hiab" + EOL + "cde", path);
    assertEquals(Collections.singletonList(expected5), split5.toTextEdit(path, source));

    // Del((47,9),"0ab\ncd\nef\ngh") + Del((44,4),"ij\nkl\nmn\nopqrstuvw") ->
    // Del((44,4),"ij\nkl\nmn\nopqrstuvw0ab\ncd\nef\ngh")
    Operation split6 =
        S(
            D(47, 9, "0ab" + EOL + "cd" + EOL + "ef" + EOL + "gh"),
            D(44, 4, "ij" + EOL + "kl" + EOL + "mn" + EOL + "opqrstuvw"));
    TextEditActivity expected6 =
        T(
            source,
            44,
            4,
            "",
            "ij"
                + EOL
                + "kl"
                + EOL
                + "mn"
                + EOL
                + "opqrstuvw0ab"
                + EOL
                + "cd"
                + EOL
                + "ef"
                + EOL
                + "gh",
            path);
    assertEquals(Collections.singletonList(expected6), split6.toTextEdit(path, source));
  }

  @Test
  public void testInsertDelete() {
    // Ins((0,5),"ab") + Del((0,5),"abcd") -> Del((0,5),"cd")
    Operation split1 = S(I(5, "ab"), D(5, "abcd"));
    TextEditActivity expected1 = T(source, 5, "", "cd", path);
    assertEquals(Collections.singletonList(expected1), split1.toTextEdit(path, source));

    // Ins((0,5),"abcde") + Del((0,5),"abcd") -> Ins((0,5),"e")
    Operation split2 = S(I(5, "abcde"), D(5, "abcd"));
    TextEditActivity expected2 = T(source, 5, "e", "", path);
    assertEquals(Collections.singletonList(expected2), split2.toTextEdit(path, source));
  }

  @Test
  public void testMultiLineInsertDeleteCase1() {
    // Ins((0,5),"ab\n") + Del((0,5),"ab\ncd") -> Del((0,5),"cd")
    Operation split1 = S(I(5, "ab" + EOL), D(5, "ab" + EOL + "cd"));
    TextEditActivity expected1 = T(source, 5, "", "cd", path);
    assertEquals(Collections.singletonList(expected1), split1.toTextEdit(path, source));

    // Ins((3,2),"ab\ncd") + Del((3,2),"ab\ncdefgh") -> Del((3,2),"efgh")
    Operation split2 = S(I(3, 2, "ab" + EOL + "cd"), D(3, 2, "ab" + EOL + "cdefgh"));
    TextEditActivity expected2 = T(source, 3, 2, "", "efgh", path);
    assertEquals(Collections.singletonList(expected2), split2.toTextEdit(path, source));

    // Ins((1,4),"ab\ncd") + Del((1,4),"ab\ncde\nfgh") -> Del((1,4),"e\nfgh")
    Operation split3 = S(I(1, 4, "ab" + EOL + "cd"), D(1, 4, "ab" + EOL + "cde" + EOL + "fgh"));
    TextEditActivity expected3 = T(source, 1, 4, "", "e" + EOL + "fgh", path);
    assertEquals(Collections.singletonList(expected3), split3.toTextEdit(path, source));

    // Ins((1,4),"ab\ncd") + Del((1,4),"ab\ncd\nefgh") -> Del((1,4),"\nefgh")
    Operation split4 = S(I(1, 4, "ab" + EOL + "cd"), D(1, 4, "ab" + EOL + "cd" + EOL + "efgh"));
    TextEditActivity expected4 = T(source, 1, 4, "", EOL + "efgh", path);
    assertEquals(Collections.singletonList(expected4), split4.toTextEdit(path, source));

    // Ins((0,0),"abc") + Del((0,0),"abcd\nefgh") -> Del((0,0),"d\nefgh")
    Operation split5 = S(I(0, "abc"), D(0, "abcd" + EOL + "efgh"));
    TextEditActivity expected5 = T(source, 0, "", "d" + EOL + "efgh", path);
    assertEquals(Collections.singletonList(expected5), split5.toTextEdit(path, source));
  }

  @Test
  public void testMultiLineInsertDeleteCase2() {
    // Ins((0,5),"ab\ncde") + Del((0,5),"ab\n") -> Ins((0,5),"cde")
    Operation split1 = S(I(5, "ab" + EOL + "cde"), D(5, "ab" + EOL));
    TextEditActivity expected1 = T(source, 5, "cde", "", path);
    assertEquals(Collections.singletonList(expected1), split1.toTextEdit(path, source));

    // Ins((1,4),"abcd\nefg") + Del((1,4),"abc") -> Ins((1,4),"d\nefg")
    Operation split2 = S(I(1, 4, "abcd" + EOL + "efg"), D(1, 4, "abc"));
    TextEditActivity expected2 = T(source, 1, 4, "d" + EOL + "efg", "", path);
    assertEquals(Collections.singletonList(expected2), split2.toTextEdit(path, source));

    // Ins((3,2),"ab\ncd\nef") + Del((3,2),"ab\ncd") -> Ins((3,2),"\ned")
    Operation split3 = S(I(3, 2, "ab" + EOL + "cd" + EOL + "ef"), D(3, 2, "ab" + EOL + "cd"));
    TextEditActivity expected3 = T(source, 3, 2, EOL + "ef", "", path);
    assertEquals(Collections.singletonList(expected3), split3.toTextEdit(path, source));
  }

  @Test
  public void testDeleteInsert() {
    // Del((0,8),"abc") + Ins((0,8),"ghijk") -> Replace "abc" with "ghijk"
    Operation split1 = S(D(8, "abc"), I(8, "ghijk"));
    TextEditActivity expected1 = T(source, 8, "ghijk", "abc", path);
    assertEquals(Collections.singletonList(expected1), split1.toTextEdit(path, source));

    // Del((1,7),"abc\ndef\ghi") + Ins((1,7),"jkl\nmno\npqr") ->
    // Replace "abc\ndef\ghi" with "jkl\nmno\npqr"
    Operation split2 =
        S(D(1, 7, "abc" + EOL + "def" + EOL + "ghi"), I(1, 7, "jkl" + EOL + "mno" + EOL + "pqr"));
    TextEditActivity expected2 =
        T(source, 1, 7, "jkl" + EOL + "mno" + EOL + "pqr", "abc" + EOL + "def" + EOL + "ghi", path);
    assertEquals(Collections.singletonList(expected2), split2.toTextEdit(path, source));
  }

  @Test
  public void testDelSplit() {
    // Split(Del((0,8),"abc"), Split(NoOperation, Ins((0,8),"ghijk")
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
