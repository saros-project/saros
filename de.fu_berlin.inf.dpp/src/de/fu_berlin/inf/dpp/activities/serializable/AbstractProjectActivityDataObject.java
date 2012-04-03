package de.fu_berlin.inf.dpp.activities.serializable;

import de.fu_berlin.inf.dpp.activities.SPathDataObject;
import de.fu_berlin.inf.dpp.net.JID;

public abstract class AbstractProjectActivityDataObject extends
    AbstractActivityDataObject implements IProjectActivityDataObject {

    protected String projectID;

    public AbstractProjectActivityDataObject(JID source) {
        super(source);
    }

    public String getProjectID() {
        return (getPath() == null) ? null : getPath().getProjectID();
    }

    public abstract SPathDataObject getPath();
}
