package de.fu_berlin.inf.dpp.activities.serializable;

import org.eclipse.swt.graphics.RGB;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import de.fu_berlin.inf.dpp.activities.business.ChangeColorActivity;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.util.xstream.JIDConverter;

/**
 * This activityDataObject wraps the ChangeColorActivity
 * 
 * @author cnk and tobi
 */
public class ChangeColorActivityDataObject extends AbstractActivityDataObject {

    @XStreamAsAttribute
    @XStreamConverter(JIDConverter.class)
    protected final JID target;
    protected final RGB color;

    public ChangeColorActivityDataObject(JID source, JID target, RGB color) {
        super(source);
        this.target = target;
        this.color = color;
    }

    public IActivity getActivity(ISarosSession sharedProject) {
        return new ChangeColorActivity(sharedProject.getUser(source),
            sharedProject.getUser(target), this.color);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((target == null) ? 0 : target.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof ChangeColorActivityDataObject))
            return false;
        ChangeColorActivityDataObject other = (ChangeColorActivityDataObject) obj;
        if (target == null) {
            if (other.target != null)
                return false;
        } else if (!target.equals(other.target))
            return false;

        return true;
    }

    public JID getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return "ChangeColorActivityDataObject(" + source + " " + target + ")";
    }
}
