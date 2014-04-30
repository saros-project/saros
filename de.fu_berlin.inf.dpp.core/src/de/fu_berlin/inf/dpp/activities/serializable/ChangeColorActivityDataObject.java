package de.fu_berlin.inf.dpp.activities.serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import de.fu_berlin.inf.dpp.activities.business.ChangeColorActivity;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.filesystem.IPathFactory;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;

/**
 * Serializable representation of a ChangeColorActivity.
 * 
 * @author cnk
 * @author tobi
 */
@XStreamAlias("changeColorActivity")
public class ChangeColorActivityDataObject extends AbstractActivityDataObject {

    @XStreamAsAttribute
    protected final User target;

    @XStreamAsAttribute
    protected final User affected;

    @XStreamAsAttribute
    protected final int colorID;

    public ChangeColorActivityDataObject(User source, User target,
        User affected, int colorID) {

        super(source);

        this.target = target;
        this.affected = affected;
        this.colorID = colorID;
    }

    /**
     * @JTourBusStop 4, Activity creation, Going back to the IActivity:
     * 
     *               When the SarosSession receives an IActivityDataObject from
     *               the ActivitySequencer it will call the method below to
     *               create an IActivity instance again. That method converts
     *               from JID instances to User instances.
     * 
     */
    /**
     * {@inheritDoc}
     */
    @Override
    public IActivity getActivity(ISarosSession session, IPathFactory pathFactory) {
        return new ChangeColorActivity(getSource(), target, affected, colorID);
    }

    @Override
    public String toString() {
        return "ChangeColorActivityDO(source: " + getSource() + ", affected: "
            + affected + ", colorID: " + colorID + ")";
    }
}
