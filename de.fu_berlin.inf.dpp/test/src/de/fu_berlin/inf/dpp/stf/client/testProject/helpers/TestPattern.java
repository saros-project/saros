package de.fu_berlin.inf.dpp.stf.client.testProject.helpers;

import java.rmi.AccessException;
import java.rmi.RemoteException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestPattern extends STFTest {

    /**
     * make sure, alice create first a java project with a class. if you don't
     * need it for you tests, please delete it.
     * 
     * @throws AccessException
     * @throws RemoteException
     * @throws InterruptedException
     */
    @BeforeClass
    public static void initMusican() throws AccessException, RemoteException,
        InterruptedException {
        /*
         * initialize the musicians simultaneously
         */
        // List<Musician> musicians =
        // InitMusician.initAliceBobCarlConcurrently();
        // alice = musicians.get(0);
        // bob = musicians.get(1);
        // carl = musicians.get(2);

        /*
         * or initialize the musicians sequentially
         */
        // alice = InitMusician.newAlice();
        // bob = InitMusician.newBob();
        // carl = InitMusician.newCarl();

        /*
         * create a java project, in which there are a class.
         */
        // alice.bot.newJavaProjectWithClass(PROJECT, PKG, CLS);

        /*
         * build session with bob and carl simultaneously
         */
        // alice.buildSessionConcurrently(PROJECT,
        // SarosConstant.CONTEXT_MENU_SHARE_PROJECT, bob, carl);

        /*
         * or build session with bob and carl sequentially
         */
        // alice.buildSessionSequential(PROJECT,
        // SarosConstant.CONTEXT_MENU_SHARE_PROJECT, bob, carl);
    }

    /**
     * make sure, all opened xmppConnects, popup windows and editor should be
     * closed. make sure, all existed projects should be deleted. if you need
     * some more after class condition for your tests, please add it.
     * 
     * @throws RemoteException
     */
    @AfterClass
    public static void resetSaros() throws RemoteException {
        resetSaros();
    }

    /**
     * make sure,all opened popup windows and editor should be closed. if you
     * need some more after condition for your tests, please add it.
     * 
     * @throws RemoteException
     */
    @After
    public void cleanUp() throws RemoteException {
        bob.remoteBot().resetWorkbench();
        carl.remoteBot().resetWorkbench();
        alice.remoteBot().resetWorkbench();
    }

    /**
     * please describe the test using the following form and change the
     * test'name. /** Steps:
     * <ol>
     * <li>step1.</li>
     * <li>step2.</li>
     * <li>step3.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>result1.</li>
     * <li>result2.</li>
     * <li>result3.</li>
     * </ol>
     * 
     * @throws RemoteException
     */

    @Test
    public void test1() throws RemoteException {
        //
    }

    /**
     * please describe the test using the following form and change the
     * test'name. /** Steps:
     * <ol>
     * <li>step1.</li>
     * <li>step2.</li>
     * <li>step3.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>result1.</li>
     * <li>result2.</li>
     * <li>result3.</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void test2() throws RemoteException {
        //
    }
}
