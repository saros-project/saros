package saros.activities;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import saros.session.User;

@XStreamAlias("gitCollectActivity")
public class GitCollectActivity extends AbstractActivity {
  @XStreamAsAttribute String basis;

  public GitCollectActivity(User source, String basis) {
    super(source);
    setBasis(basis);
  }

  @Override
  public void dispatch(IActivityReceiver receiver) {
    receiver.receive(this);
  }

  private void setBasis(String basis) {
    this.basis = basis;
  }

  public String getBasis() {
    return basis;
  }
}
