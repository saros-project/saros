package de.fu_berlin.inf.dpp.activities;

import org.eclipse.core.runtime.IPath;

public class ViewportActivity implements IActivity {
    public final int topIndex;

    public final int bottomIndex;

    private String source;

    private IPath editor;

    public ViewportActivity(int topIndex, int bottomIndex, IPath editor) {
	this.topIndex = topIndex;
	this.bottomIndex = bottomIndex;
	this.editor = editor;
    }

    public int getBottomIndex() {
	return this.bottomIndex;
    }

    public int getTopIndex() {
	return this.topIndex;
    }

    public IPath getEditor() {
	return this.editor;
    }

    @Override
    public boolean equals(Object obj) {
	if (obj instanceof ViewportActivity) {
	    ViewportActivity other = (ViewportActivity) obj;
	    return (this.topIndex == other.topIndex)
		    && (this.bottomIndex == other.bottomIndex);
	}

	return false;
    }

    @Override
    public String toString() {
	return "ViewportActivity(top:" + this.topIndex + ",bottom:"
		+ this.bottomIndex + ")";
    }

    public String getSource() {
	return this.source;
    }

    public void setSource(String source) {
	this.source = source;
    }
}
