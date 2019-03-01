package de.fu_berlin.inf.dpp.editor.text;

/** Container to hold line range info */
public class LineRange {
  private final int startLine;
  private final int numberOfLines;

  public LineRange(int startLine, int numberOfLines) {
    this.startLine = startLine;
    this.numberOfLines = numberOfLines;
  }

  public int getStartLine() {
    return startLine;
  }

  public int getNumberOfLines() {
    return numberOfLines;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof LineRange)) {
      return false;
    }

    LineRange other = (LineRange) o;

    return this.getNumberOfLines() == other.getNumberOfLines()
        && this.getStartLine() == other.getStartLine();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + startLine;
    result = prime * result + numberOfLines;
    return result;
  }
}
