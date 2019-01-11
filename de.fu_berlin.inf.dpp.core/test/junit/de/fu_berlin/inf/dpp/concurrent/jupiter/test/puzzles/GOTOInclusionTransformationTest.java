package de.fu_berlin.inf.dpp.concurrent.jupiter.test.puzzles;

import static org.junit.Assert.assertEquals;

import de.fu_berlin.inf.dpp.concurrent.jupiter.InclusionTransformation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.GOTOInclusionTransformation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.NoOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.SplitOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.test.util.JupiterTestCase;
import org.junit.Test;

public class GOTOInclusionTransformationTest extends JupiterTestCase {

  public static Operation S(Operation one, Operation two) {
    return new SplitOperation(one, two);
  }

  public static Operation I(int i, String s) {
    return new InsertOperation(i, s);
  }

  public static Operation D(int i, String s) {
    return new DeleteOperation(i, s);
  }

  protected InclusionTransformation inclusion = new GOTOInclusionTransformation();
  protected Operation insertOp = new InsertOperation(3, "abc");
  protected Operation splitOp1 =
      new SplitOperation(new DeleteOperation(2, "234"), new DeleteOperation(3, "6"));
  protected Operation splitOp2 = new SplitOperation(insertOp, new InsertOperation(7, "ins"));
  protected Operation splitOp3 = new SplitOperation(insertOp, new InsertOperation(6, "ins"));

  @Test
  public void testSplitInsertTransformation() {

    // User A:
    // 0123456
    Operation a1 = new DeleteOperation(2, "234");
    // 0156
    Operation a2 = new DeleteOperation(3, "6");
    // 015

    // User B:
    // 0123456
    Operation b1 = new InsertOperation(3, "abc");
    // 012abc3456

    // Transform Operations from A to be used by B:
    Operation newOp = inclusion.transform(new SplitOperation(a1, a2), b1, Boolean.TRUE);

    Operation expectedOp =
        new SplitOperation(
            new SplitOperation(new DeleteOperation(2, "2"), new DeleteOperation(5, "34")),
            new DeleteOperation(6, "6"));
    assertOpEquals(expectedOp, newOp);

    // Transform Operations from B to be used by A:
    newOp = inclusion.transform(b1, new SplitOperation(a1, a2), Boolean.TRUE);

    // now position 2 but origin is 3
    expectedOp = new InsertOperation(2, "abc", 3);

    assertOpEquals(expectedOp, newOp);
  }

  @Test
  public void testSplitSplitTransformation() {

    // User A:
    // 0123456
    Operation a1 = new DeleteOperation(2, "234");
    // 0156
    Operation a2 = new DeleteOperation(3, "6");
    // 015

    // User B:
    // 0123456
    Operation b1 = new InsertOperation(3, "abc");
    // 012abc3456
    Operation b2 = new InsertOperation(7, "ins");
    // 012abc3ins456

    SplitOperation a = new SplitOperation(a1, a2);
    SplitOperation b = new SplitOperation(b1, b2);

    // Result of both operation:
    // 01abcins5

    { // User B perspective:
      Operation newOp = inclusion.transform(a, b, Boolean.TRUE);
      Operation expectedOp =
          new SplitOperation(
              new SplitOperation(new DeleteOperation(2, "2"), new DeleteOperation(5, "3")),
              new SplitOperation(new DeleteOperation(8, "4"), new DeleteOperation(9, "6")));
      assertOpEquals(expectedOp, newOp);
    }

    { // User A perspective:
      Operation newOp = inclusion.transform(b, a, Boolean.TRUE);
      Operation expectedOp =
          new SplitOperation(new InsertOperation(2, "abc", 3), new InsertOperation(5, "ins", 7));
      assertOpEquals(expectedOp, newOp);
    }
  }

  public void assertOpEquals(Operation op1, Operation op2) {
    assertEquals(op1.getTextOperations(), op2.getTextOperations());
  }

  @Test
  public void testSplitSplitTransformation2() {

    // User A:
    // 0123456
    Operation a1 = new DeleteOperation(2, "234");
    // 0156
    Operation a2 = new DeleteOperation(3, "6");
    // 015

    // User B:
    // 0123456
    Operation b1 = new InsertOperation(3, "abc");
    // 012abc3456
    Operation b2 = new InsertOperation(6, "ins");
    // 012abcins3456

    SplitOperation a = new SplitOperation(a1, a2);
    SplitOperation b = new SplitOperation(b1, b2);

    // Result of both operation:
    // 01abcins5

    { // User B perspective:
      Operation newOp = inclusion.transform(a, b, Boolean.TRUE);
      Operation expectedOp =
          new SplitOperation(
              new DeleteOperation(2, "2"),
              new SplitOperation(new DeleteOperation(8, "34"), new DeleteOperation(9, "6")));
      assertOpEquals(expectedOp, newOp);
    }

    { // User A perspective:
      Operation newOp = inclusion.transform(b, a, Boolean.TRUE);
      Operation expectedOp =
          new SplitOperation(new InsertOperation(2, "abc", 3), new InsertOperation(5, "ins", 6));
      assertOpEquals(expectedOp, newOp);
    }
  }

  @Test
  public void testReplaceTransformation() {

    // abcdefghi -> abc345ghi
    Operation replace1 =
        new SplitOperation(new DeleteOperation(3, "def"), new InsertOperation(3, "345"));

    // abcdefghi -> a123defghi
    Operation replace2 =
        new SplitOperation(new InsertOperation(1, "123"), new DeleteOperation(4, "bcd"));

    // op1'(op2) (transformed op1 dependent on op2)
    Operation newOp = inclusion.transform(replace1, replace2, Boolean.TRUE);
    Operation expectedOp =
        new SplitOperation(new DeleteOperation(4, "ef"), new InsertOperation(4, "345", 3));
    assertOpEquals(expectedOp, newOp);
  }

  @Test
  public void testSplitTransformation() {

    // 0123456789 -> 01234fuenf5689
    Operation op1 =
        new SplitOperation(new DeleteOperation(7, "7"), new InsertOperation(5, "fuenf"));

    // 0123456789 -> 0123abc6789
    Operation op2 = new SplitOperation(new DeleteOperation(4, "45"), new InsertOperation(4, "abc"));

    // op1'(op2)
    Operation newOp = inclusion.transform(op1, op2, Boolean.TRUE);
    Operation expectedOp =
        new SplitOperation(new DeleteOperation(8, "7"), new InsertOperation(7, "fuenf", 5));
    assertOpEquals(expectedOp, newOp);

    // op2'(op1)
    newOp = inclusion.transform(op2, op1, Boolean.TRUE);
    expectedOp =
        new SplitOperation(
            new SplitOperation(new DeleteOperation(4, "4"), new DeleteOperation(9, "5")),
            new InsertOperation(4, "abc"));
    assertOpEquals(expectedOp, newOp);
  }

  @Test
  public void testNoOperation() {

    Operation op1 = new NoOperation();
    Operation op2 = S(D(4, "AB"), I(4, "D"));

    // op1'(op2)
    assertOpEquals(new NoOperation(), inclusion.transform(op1, op2, Boolean.FALSE));

    // op2'(op1)
    assertOpEquals(op2, inclusion.transform(op2, op1, Boolean.FALSE));
  }

  @Test
  public void testPartiallyConsumedDeleteOperation() {

    {
      Operation op1 = D(4, "ABC");
      Operation op2 = D(5, "BC");

      // op1'(op2)
      assertOpEquals(D(4, "A"), inclusion.transform(op1, op2, Boolean.FALSE));
    }
    {
      Operation op1 = D(4, "ABC");
      Operation op2 = D(4, "AB");

      // op1'(op2)
      assertOpEquals(D(4, "C"), inclusion.transform(op1, op2, Boolean.FALSE));
    }

    {
      Operation op1 = D(4, "ABC");
      Operation op2 = D(5, "BCDEF");

      // op1'(op2)
      assertOpEquals(D(4, "A"), inclusion.transform(op1, op2, Boolean.FALSE));
    }
    {
      Operation op1 = D(4, "ABC");
      Operation op2 = D(2, "89AB");

      // op1'(op2)
      assertOpEquals(D(2, "C"), inclusion.transform(op1, op2, Boolean.FALSE));
    }
  }

  @Test
  public void testTotallyConsumedDeleteOperation() {

    {
      Operation op1 = D(4, "A");
      Operation op2 = D(4, "AB");

      // op1'(op2)
      assertOpEquals(new NoOperation(), inclusion.transform(op1, op2, Boolean.FALSE));
    }
    {
      Operation op1 = D(5, "B");
      Operation op2 = D(4, "AB");

      // op1'(op2)
      assertOpEquals(new NoOperation(), inclusion.transform(op1, op2, Boolean.FALSE));
    }

    {
      Operation op1 = D(5, "B");
      Operation op2 = D(4, "ABC");

      // op1'(op2)
      assertOpEquals(new NoOperation(), inclusion.transform(op1, op2, Boolean.FALSE));
    }
  }

  @Test
  public void testNoOperationInternal() {

    /** This test causes a NoOperation to occur when transforming two SplitOperations */
    Operation op1 = S(D(4, "A"), I(4, "C"));
    Operation op2 = S(D(4, "AB"), I(4, "D"));

    // op1'(op2)
    Operation newOp = inclusion.transform(op1, op2, Boolean.FALSE);
    Operation expectedOp = new InsertOperation(5, "C", 4);
    assertOpEquals(expectedOp, newOp);
  }
}
