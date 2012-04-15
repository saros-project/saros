package de.fu_berlin.inf.dpp.activities.serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import de.fu_berlin.inf.dpp.activities.SPathDataObject;
import de.fu_berlin.inf.dpp.net.JID;

public abstract class AbstractProjectActivityDataObject extends
    AbstractActivityDataObject {

    @XStreamAlias("p")
    protected SPathDataObject path;

    public AbstractProjectActivityDataObject(JID source, SPathDataObject path) {
        super(source);
        this.path = path;
    }

    public SPathDataObject getPath() {
        return path;
    }
}
