package de.fu_berlin.inf.dpp.activities;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import de.fu_berlin.inf.dpp.session.User;
import java.io.File;

@XStreamAlias("GitSendBundleActivity")
public class GitSendBundleActivity extends AbstractActivity {
  @XStreamAsAttribute File bundleFile;

  public GitSendBundleActivity(User source, File bundleFile) {
    super(source);
    setBundleFile(bundleFile);
  }

  @Override
  public void dispatch(IActivityReceiver receiver) {
    receiver.receive(this);
  }

  private void setBundleFile(File bundleFile) {
    this.bundleFile = bundleFile;
  }

  public File getBundleFile() {
    return bundleFile;
  }
}
