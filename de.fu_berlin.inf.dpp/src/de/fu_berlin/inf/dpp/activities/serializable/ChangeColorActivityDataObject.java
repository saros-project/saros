package de.fu_berlin.inf.dpp.activities.serializable;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import de.fu_berlin.inf.dpp.activities.business.ChangeColorActivity;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.util.xstream.JIDConverter;

/**
 * Serializable representation of a ChangeColorActivity.
 * 
 * @author cnk
 * @author tobi
 */
public class ChangeColorActivityDataObject extends AbstractActivityDataObject {

    @XStreamAsAttribute
    @XStreamConverter(JIDConverter.class)
    protected final JID target;

    @XStreamAsAttribute
    @XStreamConverter(JIDConverter.class)
    protected final JID affected;

    @XStreamAsAttribute
    protected final int colorID;

    public ChangeColorActivityDataObject(JID source, JID target, JID affected,
        int colorID) {

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
    public IActivity getActivity(ISarosSession session) {
        return new ChangeColorActivity(session.getUser(getSource()),
            session.getUser(target), session.getUser(affected), colorID);
    }

    @Override
    public String toString() {
        return "ChangeColorActivityDO(source: " + getSource() + ", affected: "
            + affected + ", colorID: " + colorID + ")";
    }
}
