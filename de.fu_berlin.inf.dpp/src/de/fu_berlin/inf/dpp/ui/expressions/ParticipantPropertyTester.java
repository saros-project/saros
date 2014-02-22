package de.fu_berlin.inf.dpp.ui.expressions;

import org.eclipse.core.expressions.PropertyTester;

import de.fu_berlin.inf.dpp.session.User;

/**
 * Adds test to {@link User} instances.
 */
public class ParticipantPropertyTester extends PropertyTester {

    @Override
    public boolean test(Object receiver, String property, Object[] args,
        Object expectedValue) {
        if (receiver instanceof User) {
            User participant = (User) receiver;
            if ("hasWriteAccess".equals(property)) {
                return participant.hasWriteAccess();
            }
            if ("isRemote".equals(property)) {
                return participant.isRemote();
            }
        }
        return false;
    }

}
