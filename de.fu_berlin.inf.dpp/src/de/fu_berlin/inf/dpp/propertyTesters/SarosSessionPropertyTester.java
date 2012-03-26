package de.fu_berlin.inf.dpp.propertyTesters;

import org.eclipse.core.expressions.PropertyTester;

import de.fu_berlin.inf.dpp.project.ISarosSession;

/**
 * Adds tests to the {@link ISarosSession}. <br/>
 * Currently only tests whether the given {@link ISarosSession}'s participant is
 * the host.
 */
public class SarosSessionPropertyTester extends PropertyTester {

    public boolean test(Object receiver, String property, Object[] args,
        Object expectedValue) {
        if (receiver instanceof ISarosSession) {
            ISarosSession sarosSession = (ISarosSession) receiver;
            if ("isHost".equals(property)) {
                return sarosSession.isHost();
            }
        }
        return false;
    }

}
