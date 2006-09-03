package de.fu_berlin.inf.dpp.activities;

public class ViewportActivity implements IActivity {
    public final int topIndex;
    public final int bottomIndex;
    
    public ViewportActivity(int topIndex, int bottomIndex) {
        this.topIndex = topIndex;
        this.bottomIndex = bottomIndex;
    }

    public int getBottomIndex() {
        return bottomIndex;
    }

    public int getTopIndex() {
        return topIndex;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ViewportActivity) {
            ViewportActivity other = (ViewportActivity)obj;
            return topIndex == other.topIndex && bottomIndex == other.bottomIndex;
        }
        
        return false;
    }
    
    @Override
    public String toString() {
        return "ViewportActivity(top:"+topIndex+",bottom:"+bottomIndex+")";
    }
}
