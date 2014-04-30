package de.fu_berlin.inf.dpp.activities.serializable;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import de.fu_berlin.inf.dpp.session.User;

// TODO [MR] Add some information what needs to be done to add a new activityDataObject.
public abstract class AbstractActivityDataObject implements IActivityDataObject {

    @SuppressWarnings("unused")
    private static final Logger log = Logger
        .getLogger(AbstractActivityDataObject.class.getName());

    @XStreamAsAttribute
    protected final User source;

    public AbstractActivityDataObject(User source) {
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
        if (!(obj instanceof AbstractActivityDataObject))
            return false;

        AbstractActivityDataObject other = (AbstractActivityDataObject) obj;

        if (!ObjectUtils.equals(this.source, other.source))
            return false;

        return true;
    }
}
