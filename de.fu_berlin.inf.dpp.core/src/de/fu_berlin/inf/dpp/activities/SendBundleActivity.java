package de.fu_berlin.inf.dpp.activities;

import java.io.File;

import org.apache.commons.lang.NotImplementedException;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import de.fu_berlin.inf.dpp.session.User;

@XStreamAlias("SendBundleActivity")
public class SendBundleActivity extends AbstractActivity {
  @XStreamAsAttribute File bundleFile;

  public SendBundleActivity(User source) {
    super(source);
    // Replace with method for create Bundle after implementing JGitFacade
    throw new NotImplementedException(
        "after implementing JGit Facade replce throw Exception with create Bundle");
  }

  @Override
  public void dispatch(IActivityReceiver receiver) {
    receiver.receive(this);
  }

  public File getBundleFile() {
    return bundleFile;
  }
}
