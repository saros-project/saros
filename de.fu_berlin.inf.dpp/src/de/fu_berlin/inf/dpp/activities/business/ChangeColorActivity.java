package de.fu_berlin.inf.dpp.activities.business;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.serializable.ChangeColorActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.filesystem.IPathFactory;
import de.fu_berlin.inf.dpp.project.ISarosSession;

/**
 * Activity for managing color changes.
 * 
 * @author cnk
 * @author tobi
 * @author Stefan Rossbach
 */
public class ChangeColorActivity extends AbstractActivity implements
    ITargetedActivity {

    protected final User target;
    protected final User affected;
    protected final int colorID;

    public ChangeColorActivity(User source, User target, User affected,
        int colorID) {

        super(source);

        if (target == null)
            throw new IllegalArgumentException("target must not be null");
        if (affected == null)
            throw new IllegalArgumentException("affected must not be null");

        this.target = target;
        this.affected = affected;
        this.colorID = colorID;
    }

    @Override
    public void dispatch(IActivityReceiver receiver) {
        receiver.receive(this);
    }

    /**
     * @JTourBusStop 3, Activity creation, Creating another representation:
     * 
     *               The SarosSession will turn instances of IActivity into
     *               instances of IActivityDataObject before passing them to the
     *               ActivitySequencer. The reason for now is that the IActivity
     *               instances might use types that can not be easily
     *               serialized, e.g. an instance of a User.
     */
    /**
     * {@inheritDoc}
     */
    @Override
    public IActivityDataObject getActivityDataObject(
        ISarosSession sarosSession, IPathFactory pathFactory) {
        return new ChangeColorActivityDataObject(getSource().getJID(),
            target.getJID(), affected.getJID(), colorID);
    }

    @Override
    public String toString() {
        return "ChangeColorActivity(source: " + getSource() + ", affected: "
            + affected + ", colorID: " + colorID + ")";
    }

    /**
     * Returns the user that color id should be changed
     * 
     * @return the affected user or <code>null</code> if the user is no longer
     *         part of the session
     */
    public User getAffected() {
        return affected;
    }

    /**
     * Returns the new color id for the affected user.
     * 
     * @return the new color id
     */
    public int getColorID() {
        return colorID;
    }

    @Override
    public User getTarget() {
        return target;
    }
}
