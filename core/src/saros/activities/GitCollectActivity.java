package saros.activities;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import saros.session.User;

@XStreamAlias("gitCollectActivity")
public class GitCollectActivity extends AbstractActivity {

  public GitCollectActivity(User source) {
    super(source);
  }

  @Override
  public void dispatch(IActivityReceiver receiver) {
    receiver.receive(this);
  }
}
