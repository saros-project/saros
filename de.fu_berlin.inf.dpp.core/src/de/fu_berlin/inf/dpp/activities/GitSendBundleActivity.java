package de.fu_berlin.inf.dpp.activities;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import de.fu_berlin.inf.dpp.session.User;

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
