package de.fu_berlin.inf.dpp.activities.serializable;

import org.eclipse.jface.text.source.ILineRange;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import de.fu_berlin.inf.dpp.activities.SPathDataObject;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.ViewportActivity;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSession;

@XStreamAlias("viewportActivity")
public class ViewportActivityDataObject extends
    AbstractProjectActivityDataObject {
    @XStreamAsAttribute
    @XStreamAlias("top")
    protected final int topIndex;

    @XStreamAsAttribute
    @XStreamAlias("bottom")
    protected final int bottomIndex;

    public ViewportActivityDataObject(JID source, int topIndex,
        int bottomIndex, SPathDataObject path) {
        super(source, path);

        if (path == null) {
            throw new IllegalArgumentException("editor must not be null");
        }

        assert topIndex <= bottomIndex : "Top == " + topIndex + ", Bottom == "
            + bottomIndex;

        this.topIndex = topIndex;
        this.bottomIndex = bottomIndex;
    }

    public ViewportActivityDataObject(JID source, ILineRange viewport,
        SPathDataObject editor) {

        this(source, Math.max(0, viewport.getStartLine()), Math.max(0,
            viewport.getStartLine())
            + Math.max(0, viewport.getNumberOfLines()), editor);
    }

    @Override
    public String toString() {
        return "ViewportActivityDataObject(path:" + this.path + ",range:("
            + this.topIndex + "," + this.bottomIndex + "))";
    }

    public IActivity getActivity(ISarosSession sarosSession) {
        return new ViewportActivity(sarosSession.getUser(source), topIndex,
            bottomIndex, path.toSPath(sarosSession));
    }
}
