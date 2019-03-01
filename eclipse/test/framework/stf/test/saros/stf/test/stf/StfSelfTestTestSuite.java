package saros.stf.test.stf;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  saros.stf.test.stf.basicwidget.TestSuite.class,
  saros.stf.test.stf.chatview.TestSuite.class,
  saros.stf.test.stf.contextmenu.TestSuite.class,
  saros.stf.test.stf.editor.TestSuite.class,
  saros.stf.test.stf.internal.TestSuite.class,
  saros.stf.test.stf.menubar.TestSuite.class,
  saros.stf.test.stf.keyboard.KeyboardLayoutTest.class,
  saros.stf.test.stf.view.explorer.TestSuite.class,
  saros.stf.test.stf.view.html.TestSuite.class,
  saros.stf.test.stf.view.sarosview.TestSuite.class,
  saros.stf.test.stf.view.sarosview.content.TestSuite.class
})
public class StfSelfTestTestSuite {
  // the class remains completely empty,
  // being used only as a holder for the above annotations
}
