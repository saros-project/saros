package de.fu_berlin.inf.dpp.stf.selftest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    de.fu_berlin.inf.dpp.stf.selftest.basicwidget.TestSuite.class,
    de.fu_berlin.inf.dpp.stf.selftest.chatview.TestSuite.class,
    de.fu_berlin.inf.dpp.stf.selftest.contextmenu.TestSuite.class,
    de.fu_berlin.inf.dpp.stf.selftest.editor.TestSuite.class,
    de.fu_berlin.inf.dpp.stf.selftest.internal.TestSuite.class,
    de.fu_berlin.inf.dpp.stf.selftest.menubar.TestSuite.class,
    de.fu_berlin.inf.dpp.stf.selftest.keyboard.KeyboardLayoutTest.class,
    de.fu_berlin.inf.dpp.stf.selftest.view.explorer.TestSuite.class,
    de.fu_berlin.inf.dpp.stf.selftest.view.html.TestSuite.class,
    de.fu_berlin.inf.dpp.stf.selftest.view.sarosview.TestSuite.class,
    de.fu_berlin.inf.dpp.stf.selftest.view.sarosview.content.TestSuite.class })
public class StfSelfTestTestSuite {
    // the class remains completely empty,
    // being used only as a holder for the above annotations
}