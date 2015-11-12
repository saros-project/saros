package de.fu_berlin.inf.dpp.editor.text;

/**
 * Container to hold line range info
 */
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
}
