package saros.activities;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import saros.filesystem.IFile;
import saros.session.User;
import saros.session.internal.DeletionAcknowledgmentDispatcher;

/**
 * Activity for notifying other participants that a file was successfully deleted locally.
 *
 * @see DeletionAcknowledgmentDispatcher
 */
@XStreamAlias("deletionAcknowledgementActivity")
public class DeletionAcknowledgmentActivity extends AbstractResourceActivity {

  public DeletionAcknowledgmentActivity(User user, SPath resource) {
    super(user, resource);
  }

  @Override
  public IFile getResource() {
    return getPath().getFile();
  }

  @Override
  public void dispatch(IActivityReceiver receiver) {
    receiver.receive(this);
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + " : " + getSource() + " - " + getPath();
  }
}
