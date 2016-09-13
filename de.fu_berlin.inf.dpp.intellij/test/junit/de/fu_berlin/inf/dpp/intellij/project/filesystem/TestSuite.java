package de.fu_berlin.inf.dpp.intellij.project.filesystem;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ IntelliJFileImplTest.class, IntelliJFolderImplTest.class,
    IntelliJPathImplTest.class, IntelliJResourceImplTest.class })
public class TestSuite {
    // the class remains completely empty,
    // being used only as a holder for the above annotations
}
