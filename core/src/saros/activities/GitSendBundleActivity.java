package saros.activities;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import saros.session.User;

@XStreamAlias("gitSendBundleActivity")
public class GitSendBundleActivity extends AbstractActivity {
  @XStreamAsAttribute byte[] bundle;

  public GitSendBundleActivity(User source, byte[] bundle) {
    super(source);
    setBundle(bundle);
  }

  @Override
  public void dispatch(IActivityReceiver receiver) {
    receiver.receive(this);
  }

  private void setBundle(byte[] bundle) {
    this.bundle = bundle;
  }

  public byte[] getBundle() {
    return bundle;
  }
}
