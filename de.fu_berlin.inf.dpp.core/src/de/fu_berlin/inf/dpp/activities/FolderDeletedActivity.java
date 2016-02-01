package de.fu_berlin.inf.dpp.activities;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import de.fu_berlin.inf.dpp.session.User;

/**
 * An activity that represents the deletion of a folder made by a user during a
 * session.
 */
@XStreamAlias("folderDeleted")
public class FolderDeletedActivity extends AbstractResourceActivity implements
    IFileSystemModificationActivity {

    public FolderDeletedActivity(final User source, final SPath path) {
        super(source, path);
    }

    @Override
    public void dispatch(IActivityReceiver receiver) {
        receiver.receive(this);
    }

    @Override
    public String toString() {
        return "FolderDeletedActivity [path=" + getPath() + "]";
    }
}