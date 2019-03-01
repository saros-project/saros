package de.fu_berlin.inf.dpp.ui.expressions;

import de.fu_berlin.inf.dpp.session.ISarosSession;
import org.eclipse.core.expressions.PropertyTester;

/** Adds tests to a running {@link ISarosSession session}. */
public class SarosSessionPropertyTester extends PropertyTester {

  @Override
  public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {

    if (!(receiver instanceof ISarosSession)) return false;

    final ISarosSession session = (ISarosSession) receiver;

    if ("isHost".equals(property)) return session.isHost();

    if ("hasWriteAccess".equals(property)) return session.hasWriteAccess();

    return false;
  }
}
