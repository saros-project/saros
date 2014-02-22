package de.fu_berlin.inf.dpp.ui.expressions;

import org.eclipse.core.expressions.PropertyTester;

import de.fu_berlin.inf.dpp.project.internal.SarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSession;

/**
 * Adds tests to the {@link ISarosSession}. <br/>
 * Currently only tests whether the given {@link ISarosSession}'s participant is
 * the host.
 */
public class SarosSessionPropertyTester extends PropertyTester {

    @Override
    public boolean test(Object receiver, String property, Object[] args,
        Object expectedValue) {
        // do not check the interface as we might get the NullSarosSession
        if (receiver instanceof SarosSession) {
            ISarosSession sarosSession = (ISarosSession) receiver;
            if ("isHost".equals(property)) {
                return sarosSession.isHost();
            }
            if ("hasWriteAccess".equals(property)) {
                return sarosSession.hasWriteAccess();
            }
        }
        return false;
    }

}
