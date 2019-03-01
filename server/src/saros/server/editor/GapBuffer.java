package saros.server.editor;

/** Gap buffer implementation used by {@link Editor} for performant text edits. */
public class GapBuffer {
  // Buffer
  private char[] content;

  // Gap
  private int gapStart;
  private int gapLength;

  public GapBuffer(String content) {
    this(content, 0);
  }

  public GapBuffer(String content, int initialGap) {
    char[] contentArray = content.toCharArray();
    this.content = new char[content.length() + initialGap];
    System.arraycopy(contentArray, 0, this.content, 0, contentArray.length);
    gapStart = contentArray.length;
    gapLength = initialGap;
  }

  public void insert(int pos, String s) {
    int len = s.length();
    moveGap(pos, len);
    s.getChars(0, len, content, gapStart);
    gapStart += len;
    gapLength -= len;
  }

  public void delete(int pos, int len) {
    moveGap(pos, 0);
    gapLength += len;
  }

  public int length() {
    return content.length - gapLength;
  }

  public String toString() {
    char[] result = new char[length()];
    // copy before & after the gap
    System.arraycopy(content, 0, result, 0, gapStart);
    System.arraycopy(
        content, gapStart + gapLength, result, gapStart, content.length - gapStart - gapLength);
    return String.valueOf(result);
  }

  /**
   * Move the gap to a particular position and make sure it's at least a given size.
   *
   * @param newGapStart desired gap start (requires 0 <= newGapStart <= a.length)
   * @param minGapLength desired minimum gap length (requires 0 <= minGapLength). Effects: after
   *     this method returns, gapStart == newGapStart and gapLength >= minGapLength, but the text
   *     sequence represented by the buffer is unchanged.
   */
  private void moveGap(int newGapStart, int minGapLength) {
    char[] target;
    int newGapLength;

    if (newGapStart == gapStart && minGapLength <= gapLength) {
      return;
    }

    if (gapLength >= minGapLength) {
      // gap is big enough, use the existing array
      target = content;
      newGapLength = gapLength;
    } else {
      // need to make gap bigger, so we need a new array
      int textLength = content.length - gapLength;
      target = new char[Math.max(textLength * 2, textLength + minGapLength)];
      newGapLength = target.length - textLength;
    }

    // Now copy text from content to target.
    // Use System.arraycopy to do the copying:
    // - its fast
    // - it handles the case where content == target correctly

    // content looks like <prefix_a> <gap_a> <suffix_a>
    // target will look like <prefix_b> <gap_b> <suffix_b>
    // Let's make sure we have names for the endpoints of these ranges,
    // so we'll define gapEnd and newGapEnd:
    //    content: 0...gapStart......gapEnd......content.length
    //    target:  0...newGapStart...newGapEnd...target.length
    int gapEnd = gapStart + gapLength;
    int newGapEnd = newGapStart + newGapLength;

    // Two cases: either gap is shifting to the left or to the right.
    //  (if gap stays at the same position, either case will work.)
    if (newGapStart < gapStart) {
      // gap is shifting to the left, contents's prefix is longer than target's prefix
      //    content looks like ******___**
      //    target  looks like ***________*****
      // copy part of content's prefix, up to targets's gap
      System.arraycopy(content, 0, target, 0, newGapStart);
      // copy the rest of contents's prefix after targets's gap
      // cannot be problematic if content==target, because the gap had to be big enough
      // otherwise we would have allocated a new array
      System.arraycopy(content, newGapStart, target, newGapEnd, gapStart - newGapStart);
      // copy contents's suffix right after it
      System.arraycopy(
          content, gapEnd, target, newGapEnd + (gapStart - newGapStart), content.length - gapEnd);
    } else {
      // gap is shifting to the right, so contents's prefix is shorter than targets's prefix
      //    content looks like ***___*****
      //    target  looks like ******________**
      // copy all of content's prefix
      System.arraycopy(content, 0, target, 0, gapStart);
      // copy part of content's suffix right after it, up to target's gap
      System.arraycopy(content, gapEnd, target, gapStart, newGapStart - gapStart);
      // copy the rest of contents's suffix after target's gap
      System.arraycopy(
          content, gapEnd + (newGapStart - gapStart), target, newGapEnd, target.length - newGapEnd);
    }

    // replace the content with target
    content = target;
    gapStart = newGapStart;
    gapLength = newGapLength;
  }
}
