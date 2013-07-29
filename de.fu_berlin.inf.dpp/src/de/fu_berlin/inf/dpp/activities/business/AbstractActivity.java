package de.fu_berlin.inf.dpp.activities.business;

import org.apache.commons.lang.ObjectUtils;

import de.fu_berlin.inf.dpp.User;

// TODO [MR] Add some information what needs to be done to add a new activityDataObject.
public abstract class AbstractActivity implements IActivity {
    protected final User source;

    /**
     * @JTourBusStop 2, Activity creation, The abstract class to extend from:
     * 
     *               A new activity implementation should inherit this class.
     */

    public AbstractActivity(User source) {
        if (source == null)
            throw new IllegalArgumentException("Source cannot be null");

        this.source = source;
    }

    @Override
    public User getSource() {
        return this.source;
    }

    @Override
    public int hashCode() {
        return ObjectUtils.hashCode(source);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof AbstractActivity))
            return false;

        AbstractActivity other = (AbstractActivity) obj;

        if (!ObjectUtils.equals(this.source, other.source))
            return false;

        return true;
    }
}
