package de.fu_berlin.inf.dpp.activities.serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.ViewportActivity;
import de.fu_berlin.inf.dpp.filesystem.IPathFactory;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;

@XStreamAlias("viewportActivity")
public class ViewportActivityDataObject extends
    AbstractProjectActivityDataObject {
    @XStreamAlias("o")
    @XStreamAsAttribute
    protected final int startLine;

    @XStreamAlias("l")
    @XStreamAsAttribute
    protected final int numberOfLines;

    public ViewportActivityDataObject(User source, int startLine,
        int numberOfLines, SPath path) {

        super(source, path);

        this.startLine = startLine;
        this.numberOfLines = numberOfLines;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + startLine;
        result = prime * result + numberOfLines;
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

        if (this.startLine != other.startLine)
            return false;
        if (this.numberOfLines != other.numberOfLines)
            return false;

        return true;
    }

    @Override
    public String toString() {
        return "ViewportActivityDO(path:" + getPath() + ", range: ("
            + startLine + "," + (startLine + numberOfLines) + "))";
    }

    @Override
    public IActivity getActivity(ISarosSession sarosSession,
        IPathFactory pathFactory) {
        return new ViewportActivity(getSource(), startLine, numberOfLines,
            getPath());
    }
}
