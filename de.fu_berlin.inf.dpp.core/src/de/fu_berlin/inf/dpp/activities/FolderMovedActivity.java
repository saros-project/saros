package de.fu_berlin.inf.dpp.activities;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import de.fu_berlin.inf.dpp.session.User;

/**
 * @deprecated not used at the moment
 */
@Deprecated
@XStreamAlias("folderMoved")
public class FolderMovedActivity extends AbstractResourceActivity implements
    IFileSystemModificationActivity {

    @XStreamAlias("d")
    private final SPath destination;

    public FolderMovedActivity(final User source, final SPath origin,
        final SPath destination) {
        super(source, origin);
        this.destination = destination;
    }

    public SPath getDestination() {
        return destination;
    }

    @Override
    public void dispatch(IActivityReceiver receiver) {
        receiver.receive(this);
    }
}