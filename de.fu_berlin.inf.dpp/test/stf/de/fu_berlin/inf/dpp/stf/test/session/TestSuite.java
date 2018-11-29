package de.fu_berlin.inf.dpp.stf.test.session;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  CreatingNewFileTest.class,
  DerivedResourcesTest.class,
  EditFileThatIsNotOpenOnRemoteSideTest.class,
  EditFileThatIsNotOpenOnRemoteSideTest.class,
  EstablishSessionWithDifferentTransportModesTest.class,
  OverlappingSharingTest.class,
  ShareMultipleProjectsTest.class
})
public class TestSuite {
  // the class remains completely empty,
  // being used only as a holder for the above annotations
}
