package saros.activities;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import saros.session.User;

@XStreamAlias("gitRequestActivity")
public class GitRequestActivity extends AbstractActivity {

  public GitRequestActivity(User source) {
    super(source);
  }

  @Override
  public void dispatch(IActivityReceiver receiver) {
    receiver.receive(this);
  }
}
