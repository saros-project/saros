/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universitaet Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.fu_berlin.inf.dpp.activities.business;

import java.util.ArrayList;
import java.util.List;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.SPathDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.ChecksumErrorActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.project.ISarosSession;

/**
 * A Checksum Error is an notification sent to the host and the peers by a user
 * who wants inconsistencies to be recovered.
 * 
 * The host will reply with an ChecksumError of the same recoveryID after having
 * sent the last FileActivity (with {@link FileActivity#isRecovery()} being set
 * related to this checksum recovery.
 */
public class ChecksumErrorActivity extends AbstractActivity {

    protected List<SPath> paths;

    protected String recoveryID;

    public List<SPath> getPaths() {
        return paths;
    }

    /**
     * Each ChecksumError has a unique ID, which should be used to identify a
     * recovery session
     */
    public String getRecoveryID() {
        return recoveryID;
    }

    public ChecksumErrorActivity(User source, List<SPath> paths,
        String recoveryID) {
        super(source);
        this.paths = paths;
        this.recoveryID = recoveryID;
    }

    public void dispatch(IActivityReceiver receiver) {
        receiver.receive(this);
    }

    public IActivityDataObject getActivityDataObject(ISarosSession sarosSession) {
        ArrayList<SPathDataObject> dataObjectPaths = new ArrayList<SPathDataObject>();
        if (this.paths != null)
            for (SPath path : this.paths) {
                dataObjectPaths.add(path.toSPathDataObject(sarosSession));
            }
        return new ChecksumErrorActivityDataObject(getSource().getJID(),
            dataObjectPaths, recoveryID);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((paths == null) ? 0 : paths.hashCode());
        result = prime * result
            + ((recoveryID == null) ? 0 : recoveryID.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        ChecksumErrorActivity other = (ChecksumErrorActivity) obj;
        if (paths == null) {
            if (other.paths != null)
                return false;
        } else if (!paths.equals(other.paths))
            return false;
        if (recoveryID == null) {
            if (other.recoveryID != null)
                return false;
        } else if (!recoveryID.equals(other.recoveryID))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ChecksumError(src:" + this.getSource() + ", paths:"
            + this.paths + ", recoveryID:" + recoveryID + ")";
    }
}
