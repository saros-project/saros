package de.fu_berlin.inf.dpp.concurrent;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import de.fu_berlin.inf.dpp.activities.serializable.TextEditActivityDataObject;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.NoOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.SplitOperation;
import de.fu_berlin.inf.dpp.net.JID;

/**
 * testing SplitOperation.toTextEdit()
 */
public class SplitOperationTest extends TestCase {

    protected IPath path = new Path("path");
    protected JID source = new JID("source@server");

    public static Operation S(Operation one, Operation two) {
        return new SplitOperation(one, two);
    }

    public static Operation I(int i, String s) {
        return new InsertOperation(i, s);
    }

    public static Operation D(int i, String s) {
        return new DeleteOperation(i, s);
    }

    public static Operation nop() {
        return new NoOperation();
    }

    public void testInsertInsert() {
        // Ins(4,"0ab") + Ins(7,"cd") -> Ins(4,"0abcd")
        Operation split1 = S(I(4, "0ab"), I(7, "cd"));
        TextEditActivityDataObject expected1 = new TextEditActivityDataObject(
            source, 4, "0abcd", "", path);
        assertEquals(Collections.singletonList(expected1), split1.toTextEdit(
            path, source));

        // vice versa
        Operation split2 = S(I(6, "0ab"), I(4, "cd"));
        TextEditActivityDataObject expected2 = new TextEditActivityDataObject(
            source, 4, "cd0ab", "", path);
        assertEquals(Collections.singletonList(expected2), split2.toTextEdit(
            path, source));
    }

    public void testDeleteDelete() {
        // Del(5,"ab") + Del(5,"cde") -> Del(5,"abcde")
        Operation split1 = S(D(5, "ab"), D(5, "cde"));
        TextEditActivityDataObject expected1 = new TextEditActivityDataObject(
            source, 5, "", "abcde", path);
        assertEquals(Collections.singletonList(expected1), split1.toTextEdit(
            path, source));

        Operation split2 = S(D(5, "cde"), D(5, "ab"));
        TextEditActivityDataObject expected2 = new TextEditActivityDataObject(
            source, 5, "", "cdeab", path);
        assertEquals(Collections.singletonList(expected2), split2.toTextEdit(
            path, source));
    }

    public void testInsertDelete() {
        // Ins(5,"ab") + Del(5,"abcd") -> Del(5,"cd")
        Operation split1 = S(I(5, "ab"), D(5, "abcd"));
        TextEditActivityDataObject expected1 = new TextEditActivityDataObject(
            source, 5, "", "cd", path);
        assertEquals(Collections.singletonList(expected1), split1.toTextEdit(
            path, source));

        Operation split2 = S(I(5, "abcde"), D(5, "abcd"));
        TextEditActivityDataObject expected2 = new TextEditActivityDataObject(
            source, 5, "e", "", path);
        assertEquals(Collections.singletonList(expected2), split2.toTextEdit(
            path, source));
    }

    public void testDeleteInsert() {
        // Del(8,"abc") + Ins(8,"ghijk") -> Replace "abc" with "ghijk"
        Operation split1 = S(D(8, "abc"), I(8, "ghijk"));
        TextEditActivityDataObject expected1 = new TextEditActivityDataObject(
            source, 8, "ghijk", "abc", path);
        assertEquals(Collections.singletonList(expected1), split1.toTextEdit(
            path, source));
    }

    public void testDelSplit() {
        // Split(Del(8,"abc"), Split(NoOperation, Ins(8,"ghijk")
        Operation split = S(D(8, "abc"), S(nop(), I(8, "ghijk")));
        TextEditActivityDataObject expected = new TextEditActivityDataObject(
            source, 8, "ghijk", "abc", path);
        assertEquals(Collections.singletonList(expected), split.toTextEdit(
            path, source));
    }

    public void testSplitChain1() {
        // Split(Split(A,B), Split(C,D)) with B and C can be combined
        Operation split = S(S(D(8, "uvw"), I(2, "abcde")), S(D(2, "abcd"), D(
            15, "xyz")));

        List<TextEditActivityDataObject> expected = new LinkedList<TextEditActivityDataObject>();
        expected
            .add(new TextEditActivityDataObject(source, 8, "", "uvw", path));
        expected.add(new TextEditActivityDataObject(source, 2, "e", "", path));
        expected
            .add(new TextEditActivityDataObject(source, 15, "", "xyz", path));

        assertEquals(expected, split.toTextEdit(path, source));
    }

    public void testSplitChain2() {
        // Split(Split(A,B), Split(C,D)) with B, C and D can be combined
        Operation split = S(S(D(8, "uvw"), I(2, "abcde")), S(D(2, "abcd"), I(3,
            "xyz")));

        List<TextEditActivityDataObject> expected = new LinkedList<TextEditActivityDataObject>();
        expected
            .add(new TextEditActivityDataObject(source, 8, "", "uvw", path));
        expected
            .add(new TextEditActivityDataObject(source, 2, "exyz", "", path));

        assertEquals(expected, split.toTextEdit(path, source));

    }

    public void testSplitChain3() {
        // Split(Split(A,B), Split(C,D)) with A, B, C and D can be combined
        Operation split = S(S(D(8, "abc"), D(8, "defg")), S(I(8, "1234"), I(12,
            "56")));

        List<TextEditActivityDataObject> expected = new LinkedList<TextEditActivityDataObject>();
        expected.add(new TextEditActivityDataObject(source, 8, "123456",
            "abcdefg", path));

        assertEquals(expected, split.toTextEdit(path, source));

    }
}
