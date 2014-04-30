package de.fu_berlin.inf.dpp.activities.serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.ShareConsoleActivity;
import de.fu_berlin.inf.dpp.session.User;

@XStreamAlias("shareConsoleActivity")
public class ShareConsoleActivityDataObject extends AbstractActivityDataObject {

    private String consoleContent;

    public ShareConsoleActivityDataObject(User source, String content) {
        super(source);

        this.consoleContent = content;
    }

    @Override
    public IActivity getActivity() {
        return new ShareConsoleActivity(getSource(), consoleContent);
    }
}
