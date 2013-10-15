package de.fu_berlin.inf.dpp.ui.expressions;

import java.util.Collection;

import org.eclipse.core.expressions.PropertyTester;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;

/**
 * Adds tests to the {@link JID}. <br/>
 * Currently only tests whether given {@link JID} is part of the
 * {@link ISarosSession}.
 */
public class BuddyPropertyTester extends PropertyTester {

    @Inject
    ISarosSessionManager sarosSessionManager;

    public BuddyPropertyTester() {
        SarosPluginContext.initComponent(this);
    }

    @Override
    public boolean test(Object receiver, String property, Object[] args,
        Object expectedValue) {
        if (receiver instanceof JID) {
            JID jid = (JID) receiver;
            if ("isInSarosSession".equals(property)) {
                ISarosSession sarosSession = sarosSessionManager
                    .getSarosSession();
                if (sarosSession != null) {
                    Collection<User> users = sarosSession.getUsers();
                    for (User user : users) {
                        if (user.getJID().equals(jid)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

}
