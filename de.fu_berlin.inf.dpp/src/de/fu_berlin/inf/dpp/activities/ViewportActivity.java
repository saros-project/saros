package de.fu_berlin.inf.dpp.activities;

public class ViewportActivity implements IActivity {
    public final int bottomIndex;

    private String source;

    public final int topIndex;

    public ViewportActivity(int topIndex, int bottomIndex) {
	this.topIndex = topIndex;
	this.bottomIndex = bottomIndex;
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

    public int getBottomIndex() {
	return this.bottomIndex;
    }

    public String getSource() {
	return this.source;
    }

    public int getTopIndex() {
	return this.topIndex;
    }

    public void setSource(String source) {
	this.source = source;
    }

    @Override
    public String toString() {
	return "ViewportActivity(top:" + this.topIndex + ",bottom:"
		+ this.bottomIndex + ")";
    }
}
