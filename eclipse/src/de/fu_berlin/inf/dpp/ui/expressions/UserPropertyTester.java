package de.fu_berlin.inf.dpp.ui.expressions;

import de.fu_berlin.inf.dpp.session.User;
import org.eclipse.core.expressions.PropertyTester;

/** Adds test to {@link User} instances. */
public class UserPropertyTester extends PropertyTester {

  @Override
  public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {

    if (!(receiver instanceof User)) return false;

    final User user = (User) receiver;

    if ("hasWriteAccess".equals(property)) return user.hasWriteAccess();

    if ("isRemote".equals(property)) return user.isRemote();

    return false;
  }
}
