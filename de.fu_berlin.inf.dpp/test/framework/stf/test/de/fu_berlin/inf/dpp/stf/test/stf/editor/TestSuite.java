package de.fu_berlin.inf.dpp.stf.test.stf.editor;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  EditorWithoutSessionTest.class,
  EditorByAliceBobTest.class,
  EditorByAliceTest.class
})
public class TestSuite {
  // the class remains completely empty,
  // being used only as a holder for the above annotations
}
