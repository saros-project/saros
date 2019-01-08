package de.fu_berlin.inf.dpp.activities;

import java.io.File;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import de.fu_berlin.inf.dpp.git.JGitFacade;
import de.fu_berlin.inf.dpp.session.SarosSessionManager;
import de.fu_berlin.inf.dpp.session.User;

@XStreamAlias("SendBundleActivity")
public class SendBundleActivity extends AbstractActivity {
    @XStreamAsAttribute
    File bundleFile;

    public sendBundle(User source, File workDir, String actual, String basis) {
        super(source);
        this.bundleFile = JGitFacade.createBundle(workDir,actual,basis);
    }

    @Override
    public void dispatch(IActivityReceiver receiver) {
        receiver.receive(this);
    }

    public String getBundleFile() {
        return bundleFile;
    }

}
