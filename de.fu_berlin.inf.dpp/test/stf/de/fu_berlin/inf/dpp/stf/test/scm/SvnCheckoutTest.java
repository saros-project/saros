package de.fu_berlin.inf.dpp.stf.test.scm;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;
import de.fu_berlin.inf.dpp.stf.test.Constants;

public class SvnCheckoutTest extends StfTestCase {

    @BeforeClass
    public static void selectTesters() throws Exception {
        select(ALICE, BOB);
    }

    @Test
    @Ignore("Asks for a password -.-")
    public void testSvnCheckout() throws Exception {
        ALICE.superBot().views().packageExplorerView().tree().newC()
            .javaProject(Constants.SVN_PROJECT_COPY);

        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .selectProject(Constants.SVN_PROJECT_COPY)
            .team()
            .shareProjectUsingSpecifiedFolderName(Constants.SVN_REPOSITORY_URL,
                Constants.SVN_PROJECT_PATH);

        Util.buildSessionSequentially(Constants.SVN_PROJECT_COPY,
            TypeOfCreateProject.NEW_PROJECT, ALICE, BOB);

    }
}
