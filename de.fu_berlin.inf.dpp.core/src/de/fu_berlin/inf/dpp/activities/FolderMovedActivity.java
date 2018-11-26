package de.fu_berlin.inf.dpp.activities;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import de.fu_berlin.inf.dpp.session.User;

/** @deprecated not used at the moment */
@Deprecated
@XStreamAlias("folderMoved")
public class FolderMovedActivity extends AbstractResourceActivity
    implements IFileSystemModificationActivity {

  @XStreamAlias("d")
  private final SPath destination;

  public FolderMovedActivity(final User source, final SPath origin, final SPath destination) {
    super(source, origin);

    if (origin == null) throw new IllegalArgumentException("origin must not be null");

    if (destination == null) throw new IllegalArgumentException("destination must not be null");

    this.destination = destination;
  }

  /**
   * Returns the destination path to move the original folder to.
   *
   * @see #getPath()
   */
  public SPath getDestination() {
    return destination;
  }

  @Override
  public boolean isValid() {
    return super.isValid() && (getPath() != null) && destination != null;
  }

  @Override
  public void dispatch(IActivityReceiver receiver) {
    receiver.receive(this);
  }
}
