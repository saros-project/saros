package saros.concurrent.jupiter.test.puzzles;

import static org.junit.Assert.assertEquals;
import static saros.test.util.OperationHelper.D;
import static saros.test.util.OperationHelper.I;
import static saros.test.util.OperationHelper.NOP;
import static saros.test.util.OperationHelper.S;

import org.junit.Test;
import saros.concurrent.jupiter.InclusionTransformation;
import saros.concurrent.jupiter.Operation;
import saros.concurrent.jupiter.internal.text.GOTOInclusionTransformation;
import saros.concurrent.jupiter.internal.text.SplitOperation;
import saros.concurrent.jupiter.test.util.JupiterTestCase;

public class GOTOInclusionTransformationTest extends JupiterTestCase {

  protected InclusionTransformation inclusion = new GOTOInclusionTransformation();

  @Test
  public void testSplitInsertTransformation() {

    // User A:
    // 0123456
    Operation a1 = D(2, "234");
    // 0156
    Operation a2 = D(3, "6");
    // 015

    // User B:
    // 0123456
    Operation b1 = I(3, "abc");
    // 012abc3456

    // Transform Operations from A to be used by B:
    Operation newOp = inclusion.transform(S(a1, a2), b1, Boolean.TRUE);

    Operation expectedOp = S(S(D(2, "2"), D(5, "34")), D(6, "6"));
    assertOpEquals(expectedOp, newOp);

    // Transform Operations from B to be used by A:
    newOp = inclusion.transform(b1, S(a1, a2), Boolean.TRUE);

    // now position 2 but origin is 3
    expectedOp = I(2, "abc", 3);

    assertOpEquals(expectedOp, newOp);
  }

  @Test
  public void testSplitSplitTransformation() {

    // User A:
    // 0123456
    Operation a1 = D(2, "234");
    // 0156
    Operation a2 = D(3, "6");
    // 015

    // User B:
    // 0123456
    Operation b1 = I(3, "abc");
    // 012abc3456
    Operation b2 = I(7, "ins");
    // 012abc3ins456

    SplitOperation a = S(a1, a2);
    SplitOperation b = S(b1, b2);

    // Result of both operation:
    // 01abcins5

    { // User B perspective:
      Operation newOp = inclusion.transform(a, b, Boolean.TRUE);
      Operation expectedOp = S(S(D(2, "2"), D(5, "3")), S(D(8, "4"), D(9, "6")));
      assertOpEquals(expectedOp, newOp);
    }

    { // User A perspective:
      Operation newOp = inclusion.transform(b, a, Boolean.TRUE);
      Operation expectedOp = S(I(2, "abc", 3), I(5, "ins", 7));
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
    Operation a1 = D(2, "234");
    // 0156
    Operation a2 = D(3, "6");
    // 015

    // User B:
    // 0123456
    Operation b1 = I(3, "abc");
    // 012abc3456
    Operation b2 = I(6, "ins");
    // 012abcins3456

    SplitOperation a = S(a1, a2);
    SplitOperation b = S(b1, b2);

    // Result of both operation:
    // 01abcins5

    { // User B perspective:
      Operation newOp = inclusion.transform(a, b, Boolean.TRUE);
      Operation expectedOp = S(D(2, "2"), S(D(8, "34"), D(9, "6")));
      assertOpEquals(expectedOp, newOp);
    }

    { // User A perspective:
      Operation newOp = inclusion.transform(b, a, Boolean.TRUE);
      Operation expectedOp = S(I(2, "abc", 3), I(5, "ins", 6));
      assertOpEquals(expectedOp, newOp);
    }
  }

  @Test
  public void testReplaceTransformation() {

    // abcdefghi -> abc345ghi
    Operation replace1 = S(D(3, "def"), I(3, "345"));

    // abcdefghi -> a123efghi
    Operation replace2 = S(I(1, "123"), D(4, "bcd"));

    // op1'(op2) (transformed op1 dependent on op2)
    Operation newOp = inclusion.transform(replace1, replace2, Boolean.TRUE);
    Operation expectedOp = S(D(4, "ef"), I(4, "345", 3));
    assertOpEquals(expectedOp, newOp);
  }

  @Test
  public void testSplitTransformation() {

    // 0123456789 -> 01234fuenf5689
    Operation op1 = S(D(7, "7"), I(5, "fuenf"));

    // 0123456789 -> 0123abc6789
    Operation op2 = S(D(4, "45"), I(4, "abc"));

    // op1'(op2)
    Operation newOp = inclusion.transform(op1, op2, Boolean.TRUE);
    Operation expectedOp = S(D(8, "7"), I(7, "fuenf", 5));
    assertOpEquals(expectedOp, newOp);

    // op2'(op1)
    newOp = inclusion.transform(op2, op1, Boolean.TRUE);
    expectedOp = S(S(D(4, "4"), D(9, "5")), I(4, "abc"));
    assertOpEquals(expectedOp, newOp);
  }

  @Test
  public void testNoOperation() {

    Operation op1 = NOP();
    Operation op2 = S(D(4, "AB"), I(4, "D"));

    // op1'(op2)
    assertOpEquals(NOP(), inclusion.transform(op1, op2, Boolean.FALSE));

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
    {
      Operation op1 = D(4, "ABCDEF");
      Operation op2 = D(5, "BCD");

      // op1'(op2)
      assertOpEquals(D(4, "AEF"), inclusion.transform(op1, op2, Boolean.FALSE));
    }
  }

  @Test
  public void testTotallyConsumedDeleteOperation() {

    {
      Operation op1 = D(4, "A");
      Operation op2 = D(4, "AB");

      // op1'(op2)
      assertOpEquals(NOP(), inclusion.transform(op1, op2, Boolean.FALSE));
    }
    {
      Operation op1 = D(5, "B");
      Operation op2 = D(4, "AB");

      // op1'(op2)
      assertOpEquals(NOP(), inclusion.transform(op1, op2, Boolean.FALSE));
    }

    {
      Operation op1 = D(5, "B");
      Operation op2 = D(4, "ABC");

      // op1'(op2)
      assertOpEquals(NOP(), inclusion.transform(op1, op2, Boolean.FALSE));
    }
  }

  @Test
  public void testNoOperationInternal() {

    /* This test causes a NoOperation to occur when transforming two SplitOperations */
    Operation op1 = S(D(4, "A"), I(4, "C"));
    Operation op2 = S(D(4, "AB"), I(4, "D"));

    // op1'(op2)
    Operation newOp = inclusion.transform(op1, op2, Boolean.FALSE);
    Operation expectedOp = I(5, "C", 4);
    assertOpEquals(expectedOp, newOp);
  }
}
