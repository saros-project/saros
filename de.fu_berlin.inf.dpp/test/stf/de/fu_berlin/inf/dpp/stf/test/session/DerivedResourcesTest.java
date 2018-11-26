package de.fu_berlin.inf.dpp.stf.test.session;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static org.junit.Assert.assertFalse;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.shared.Constants;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class DerivedResourcesTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB);
  }

  /*
   * this test is testing the logic, not the attached icons to shared
   * resources !
   */
  @Test
  @Ignore(
      "Does not work under *NIX because the context menu does not contain the Buidl Project option")
  public void testDerivedResourcesMustNotMarkedAsSharedInAFullSharedProject() throws Exception {

    Util.setUpSessionWithJavaProjectAndClass("Foo", "bar", "Foo", ALICE, BOB);

    BOB.superBot().views().packageExplorerView().waitUntilResourceIsShared("Foo/src/bar/Foo.java");

    ALICE.remoteBot().view(Constants.VIEW_PACKAGE_EXPLORER).show();

    ALICE
        .remoteBot()
        .view(Constants.VIEW_PACKAGE_EXPLORER)
        .bot()
        .tree()
        .selectTreeItemWithRegex("Foo.*")
        .clickContextMenu("Build Project");

    /*
     * the following STF functions does not use the GUI, so we can ignore
     * the current package explorer content
     */

    ALICE.superBot().views().packageExplorerView().waitUntilFileExists("Foo/bin/bar/Foo.class");

    assertFalse(
        "compiled class file is marked as shared",
        ALICE.superBot().views().packageExplorerView().isResourceShared("Foo/bin/bar/Foo.class"));
  }
}
