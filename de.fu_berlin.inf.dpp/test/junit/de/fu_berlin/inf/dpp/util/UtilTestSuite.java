package de.fu_berlin.inf.dpp.util;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import de.fu_berlin.inf.dpp.test.util.EclipseWorkspaceFakeFacadeTest;
import de.fu_berlin.inf.dpp.test.util.WorkspaceFakeObjectTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({ UtilTest.class, ArrayIteratorTest.class,
    NamedThreadFactoryTest.class, PairTest.class,
    TotalOrderComparatorTest.class, ArrayUtilsTest.class,
    MappingIteratorTest.class, EclipseWorkspaceFakeFacadeTest.class,
    WorkspaceFakeObjectTest.class, ActivityUtilsTest.class,
    ThreadAccessRecorderTest.class, })
public class UtilTestSuite {
    // the class remains completely empty,
    // being used only as a holder for the above annotations
}
