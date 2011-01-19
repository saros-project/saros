package de.fu_berlin.inf.dpp.activities.serializable;

import de.fu_berlin.inf.dpp.activities.SPathDataObject;
import de.fu_berlin.inf.dpp.net.JID;

/**
 * There are ActiviDataObjects that are related to a specific project. Every
 * ActivityDataObject which is related to a specific project must extend this
 * class instead of {@link AbstractActivityDataObject} or implement the
 * {@link IProjectActivityDataObject} so Saros can filter them if the addressed
 * project doesn't exist yet.
 */
public abstract class AbstractProjectActivityDataObject extends
    AbstractActivityDataObject implements IProjectActivityDataObject {

    protected String projectID;

    public AbstractProjectActivityDataObject(JID source) {
        super(source);
    }

    public String getProjectID() {
        return getPath().getProjectID();
    }

    public abstract SPathDataObject getPath();
}
