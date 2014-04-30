package de.fu_berlin.inf.dpp.activities.serializable;

import org.apache.commons.lang.ObjectUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.business.FileActivity.Purpose;
import de.fu_berlin.inf.dpp.activities.business.FileActivity.Type;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.RecoveryFileActivity;
import de.fu_berlin.inf.dpp.filesystem.IPathFactory;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;

/**
 * Subclass of FileActivityDataObject that is used during the Recovery-Process
 * and allows the specification of targets. This Activity will is sent from the
 * host to the client that requested the recovery.
 */
@XStreamAlias("recoveryFileActivity")
public class RecoveryFileActivityDataObject extends FileActivityDataObject
    implements IActivityDataObject {

    @XStreamAsAttribute
    protected User target;

    public RecoveryFileActivityDataObject(User source, User target, Type type,
        SPath newPath, SPath oldPath, byte[] data) {

        super(source, type, newPath, oldPath, data, Purpose.RECOVERY);

        this.target = target;
    }

    @Override
    public IActivity getActivity(ISarosSession sarosSession,
        IPathFactory pathFactory) {
        return new RecoveryFileActivity(getSource(), target, getType(), path,
            oldPath, data);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ObjectUtils.hashCode(target);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof RecoveryFileActivityDataObject))
            return false;

        RecoveryFileActivityDataObject other = (RecoveryFileActivityDataObject) obj;

        if (!ObjectUtils.equals(this.target, other.target))
            return false;

        return true;
    }
}
