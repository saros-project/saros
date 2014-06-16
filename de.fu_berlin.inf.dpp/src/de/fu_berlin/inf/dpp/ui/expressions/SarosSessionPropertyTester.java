package de.fu_berlin.inf.dpp.ui.expressions;

import org.eclipse.core.expressions.PropertyTester;

import de.fu_berlin.inf.dpp.serviceProviders.NullSarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSession;

/**
 * Adds tests to a running {@link ISarosSession session}.
 */
public class SarosSessionPropertyTester extends PropertyTester {

    @Override
    public boolean test(Object receiver, String property, Object[] args,
        Object expectedValue) {
        // do not check the interface as we might get the NullSarosSession
        if (receiver instanceof NullSarosSession)
            return false;

        final ISarosSession session = (ISarosSession) receiver;

        if ("isHost".equals(property))
            return session.isHost();

        if ("hasWriteAccess".equals(property))
            return session.hasWriteAccess();

        return false;
    }

}
