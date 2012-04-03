package de.fu_berlin.inf.dpp.activities.serializable;

import de.fu_berlin.inf.dpp.activities.SPathDataObject;
import de.fu_berlin.inf.dpp.net.JID;

public abstract class AbstractProjectActivityDataObject extends
    AbstractActivityDataObject implements IProjectActivityDataObject {

    protected SPathDataObject path;

    public AbstractProjectActivityDataObject(JID source, SPathDataObject path) {
        super(source);
        this.path = path;
    }

    public String getProjectID() {
        return (getPath() == null) ? null : getPath().getProjectID();
    }

    public SPathDataObject getPath() {
        return path;
    }
}
