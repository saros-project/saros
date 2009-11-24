package de.fu_berlin.inf.dpp.activities.business;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.User;

// TODO [MR] Add some information what needs to be done to add a new activityDataObject.
public abstract class AbstractActivity implements IActivity {

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger(AbstractActivity.class
        .getName());

    protected final User source;

    public AbstractActivity(User source) {
        if (source == null)
            throw new IllegalArgumentException("Source cannot be null");
        this.source = source;
    }

    public User getSource() {
        return this.source;
    }

    @Override
    public int hashCode() {
        return (source == null) ? 0 : source.hashCode();
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
        return ObjectUtils.equals(this.source, other.source);
    }
}
