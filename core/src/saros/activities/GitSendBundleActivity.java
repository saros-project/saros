package saros.activities;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import saros.session.User;

@XStreamAlias("gitSendBundleActivity")
public class GitSendBundleActivity extends AbstractActivity {

  public GitSendBundleActivity(User source) {
    super(source);
  }

  @Override
  public void dispatch(IActivityReceiver receiver) {
    receiver.receive(this);
  }
}
