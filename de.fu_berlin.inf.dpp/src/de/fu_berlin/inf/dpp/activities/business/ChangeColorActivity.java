package de.fu_berlin.inf.dpp.activities.business;

import org.eclipse.swt.graphics.RGB;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.serializable.ChangeColorActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.project.ISarosSession;

/**
 * This activity holds the color of the source.
 * 
 * @author cnk and tobi
 */
public class ChangeColorActivity extends AbstractActivity {

    protected final User target;
    protected final RGB color;

    public ChangeColorActivity(User source, User target, RGB color) {
        super(source);
        this.target = target;
        this.color = color;
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
    public IActivityDataObject getActivityDataObject(ISarosSession sarosSession) {
        return new ChangeColorActivityDataObject(source.getJID(),
            target.getJID(), this.color);
    }

    @Override
    public String toString() {
        return "ChangeColorActivity(" + source + " " + target + " " + color
            + ")";
    }

    public User getTarget() {
        return target;
    }

    public RGB getColor() {
        return this.color;
    }
}
