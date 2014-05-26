package de.fu_berlin.inf.dpp.activities;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import de.fu_berlin.inf.dpp.session.User;

/**
 * A ShareConsoleActivity is used to transmit text from a user to all other
 * participants of the running session.
 */
@XStreamAlias("shareConsoleActivity")
public class ShareConsoleActivity extends AbstractActivity {

    private final String consoleContent;

    public ShareConsoleActivity(User source, String content) {
        super(source);

        this.consoleContent = content;
    }

    @Override
    public void dispatch(IActivityReceiver receiver) {
        receiver.receive(this);
    }

    public String getConsoleContent() {
        return consoleContent;
    }

}
