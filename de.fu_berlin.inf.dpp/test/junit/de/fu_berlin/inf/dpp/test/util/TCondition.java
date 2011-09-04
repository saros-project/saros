package de.fu_berlin.inf.dpp.test.util;

/**
 * A TCondition can be used with
 * {@link de.fu_berlin.inf.dpp.test.util.SarosTestUtils#waitFor(TCondition)}
 * to check iteratively the fulfillment of it.
 *
 * @author cordes
 */
public abstract class TCondition {

    abstract public boolean isFullfilled() throws Exception;

    abstract public String getFailureMessage();
}
