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
public class DeletionAcknowledgmentActivity extends AbstractResourceActivity<IFile> {

  public DeletionAcknowledgmentActivity(User user, IFile file) {
    super(user, file);
  }

  @Override
  public void dispatch(IActivityReceiver receiver) {
    receiver.receive(this);
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + " : " + getSource() + " - " + getResource();
  }
}
