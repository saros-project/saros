package de.fu_berlin.inf.dpp.activities;

import java.io.File;
import org.apache.commons.lang3.NotImplementedException;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import de.fu_berlin.inf.dpp.git.JGitFacade;
import de.fu_berlin.inf.dpp.session.SarosSessionManager;
import de.fu_berlin.inf.dpp.session.User;

@XStreamAlias("SendBundleActivity")
public class SendBundleActivity extends AbstractActivity {
  @XStreamAsAttribute File bundleFile;

  public sendBundle(User source, File workDir, String actual, String basis) {
    super(source);
    // Replace with
    // this.bundleFile = JGitFacade.createBundle(workDir,actual,basis);
    // as soon as the Facade is implemented
    throw new NotImplementedException(
        "after implementing JGitFacade replace throw Exception with createBundle");
  }

  @Override
  public void dispatch(IActivityReceiver receiver) {
    receiver.receive(this);
  }

  public String getBundleFile() {
    return bundleFile;
  }
}
