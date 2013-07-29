package de.fu_berlin.inf.dpp.activities.business;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.jface.text.source.LineRange;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.ViewportActivityDataObject;
import de.fu_berlin.inf.dpp.project.ISarosSession;

public class ViewportActivity extends AbstractActivity implements
    IResourceActivity {

    protected final int topIndex;
    protected final int bottomIndex;
    protected final SPath path;

    public ViewportActivity(User source, int topIndex, int bottomIndex,
        SPath path) {

        super(source);

        if (path == null)
            throw new IllegalArgumentException("path must not be null");

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

    @Override
    public SPath getPath() {
        return this.path;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + bottomIndex;
        result = prime * result + ObjectUtils.hashCode(path);
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

        if (this.topIndex != other.topIndex)
            return false;
        if (this.bottomIndex != other.bottomIndex)
            return false;
        if (!ObjectUtils.equals(this.path, other.path))
            return false;

        return true;
    }

    @Override
    public String toString() {
        return "ViewportActivity(path: " + path + ", range: (" + topIndex + ","
            + bottomIndex + "))";
    }

    @Override
    public void dispatch(IActivityReceiver receiver) {
        receiver.receive(this);
    }

    @Override
    public IActivityDataObject getActivityDataObject(ISarosSession sarosSession) {
        return new ViewportActivityDataObject(getSource().getJID(), topIndex,
            bottomIndex, path.toSPathDataObject(sarosSession));
    }
}
