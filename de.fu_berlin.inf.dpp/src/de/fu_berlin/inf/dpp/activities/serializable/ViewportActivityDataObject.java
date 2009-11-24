package de.fu_berlin.inf.dpp.activities.serializable;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.jface.text.source.LineRange;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.ViewportActivity;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISharedProject;

@XStreamAlias("viewportActivity")
public class ViewportActivityDataObject extends AbstractActivityDataObject {
    @XStreamAsAttribute
    @XStreamAlias("top")
    protected final int topIndex;

    @XStreamAsAttribute
    @XStreamAlias("bottom")
    protected final int bottomIndex;

    @XStreamAsAttribute
    protected final IPath path;

    public ViewportActivityDataObject(JID source, int topIndex,
        int bottomIndex, IPath path) {
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

    public ViewportActivityDataObject(JID source, ILineRange viewport,
        IPath editor) {
        this(source, Math.max(0, viewport.getStartLine()), Math.max(0, viewport
            .getStartLine())
            + Math.max(0, viewport.getNumberOfLines()), editor);
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

    public IPath getEditor() {
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
        if (!(obj instanceof ViewportActivityDataObject))
            return false;
        ViewportActivityDataObject other = (ViewportActivityDataObject) obj;
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
        return "ViewportActivityDataObject(path:" + this.path + ",range:("
            + this.topIndex + "," + this.bottomIndex + "))";
    }

    public boolean dispatch(IActivityDataObjectConsumer consumer) {
        return consumer.consume(this);
    }

    public void dispatch(IActivityDataObjectReceiver receiver) {
        receiver.receive(this);
    }

    public IActivity getActivity(ISharedProject sharedProject) {
        return new ViewportActivity(sharedProject.getUser(source), topIndex,
            bottomIndex, path);
    }
}
