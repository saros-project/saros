package de.fu_berlin.inf.dpp.net.internal;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import de.fu_berlin.inf.dpp.net.internal.extensions.InternalExtensionsTestSuite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ TransferDescriptionTest.class, SarosNetConnectTest.class,
    SubscriptionManagerTest.class, SarosNetTest.class, UserTest.class,
    InternalExtensionsTestSuite.class })
public class InternalTestSuite {
    // the class remains completely empty,
    // being used only as a holder for the above annotations
}
