package de.fu_berlin.inf.dpp.activities.business;

import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.jface.text.source.LineRange;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.ViewportActivityDataObject;
import de.fu_berlin.inf.dpp.project.ISarosSession;

public class ViewportActivity extends AbstractActivity {

    protected final int topIndex;
    protected final int bottomIndex;
    protected final SPath path;

    public ViewportActivity(User source, int topIndex, int bottomIndex,
        SPath path) {

        super(source);

        if (path == null) {
            throw new IllegalArgumentException("editor must not be null");
        }

        assert topIndex <= bottomIndex : "Top == " + topIndex + ", Bottom == "
            + bottomIndex;

        this.topIndex = topIndex;
        this.bottomIndex = bottomIndex;
        this.path = path;
    }

    public ViewportActivity(User source, ILineRange viewport, SPath path) {

        this(source, Math.max(0, viewport.getStartLine()), Math.max(0,
            viewport.getStartLine())
            + Math.max(0, viewport.getNumberOfLines()), path);
    }

    public ILineRange getLineRange() {
        return new LineRange(topIndex, bottomIndex - topIndex);
    }

    public int getBottomIndex() {
        return this.bottomIndex;
    }

    public int getTopIndex() {
        return this.topIndex;
    }

    public SPath getPath() {
        return this.path;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + bottomIndex;
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + topIndex;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof ViewportActivity))
            return false;
        ViewportActivity other = (ViewportActivity) obj;
        if (bottomIndex != other.bottomIndex)
            return false;
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        if (topIndex != other.topIndex)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ViewportActivity(path:" + this.path + ",range:("
            + this.topIndex + "," + this.bottomIndex + "))";
    }

    public void dispatch(IActivityReceiver receiver) {
        receiver.receive(this);
    }

    public IActivityDataObject getActivityDataObject(ISarosSession sarosSession) {
        return new ViewportActivityDataObject(source.getJID(), topIndex,
            bottomIndex, path.toSPathDataObject(sarosSession));
    }
}
