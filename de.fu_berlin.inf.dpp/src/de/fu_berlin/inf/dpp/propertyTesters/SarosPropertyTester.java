package de.fu_berlin.inf.dpp.propertyTesters;

import org.eclipse.core.expressions.PropertyTester;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.project.ISarosSession;

/**
 * Adds tests to the {@link ISarosSession}. <br/>
 * Currently only tests whether the given {@link ISarosSession}'s participant is
 * the host.
 */
public class SarosPropertyTester extends PropertyTester {

    public boolean test(Object receiver, String property, Object[] args,
        Object expectedValue) {
        if (receiver instanceof Saros) {
            Saros saros = (Saros) receiver;
            if ("isConnected".equals(property)) {
                return saros.isConnected();
            }
        }
        return false;
    }

}
