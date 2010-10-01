package de.fu_berlin.inf.dpp.stf.test;

import java.rmi.AccessException;
import java.rmi.RemoteException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.sarosswtbot.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.Musician;

public class TestPattern {
    private static final String PROJECT = BotConfiguration.PROJECTNAME;
    private static final String PROJECT2 = BotConfiguration.PROJECTNAME2;
    private static final String PROJECT3 = BotConfiguration.PROJECTNAME3;
    private static final String PKG = BotConfiguration.PACKAGENAME;
    private static final String PKG2 = BotConfiguration.PACKAGENAME2;
    private static final String PKG3 = BotConfiguration.PACKAGENAME3;

    private static final String CLS = BotConfiguration.CLASSNAME;
    private static final String CLS2 = BotConfiguration.CLASSNAME2;
    private static final String CLS3 = BotConfiguration.CLASSNAME3;

    private static final String CP = BotConfiguration.CONTENTPATH;
    private static final String CP2 = BotConfiguration.CONTENTPATH2;
    private static final String CP3 = BotConfiguration.CONTENTPATH3;

    protected static Musician alice = InitMusician.newAlice();
    protected static Musician bob = InitMusician.newBob();
    protected static Musician carl = InitMusician.newCarl();

    /**
     * make sure, alice create first a java project with a class. if you don't
     * need it for you tests, please delete it.
     * 
     * @throws AccessException
     * @throws RemoteException
     */
    @BeforeClass
    public static void beforeClass() throws AccessException, RemoteException {
        alice.bot.newJavaProjectWithClass(PROJECT, PKG, CLS);
    }

    /**
     * make sure, all opened xmppConnects, popup windows and editor should be
     * closed. make sure, all existed projects should be deleted. if you need
     * some more after class condition for your tests, please add it.
     * 
     * @throws RemoteException
     */
    @AfterClass
    public static void afterClass() throws RemoteException {
        bob.bot.resetSaros();
        carl.bot.resetSaros();
        alice.bot.resetSaros();
    }

    /**
     * make sure,all opened popup windows and editor should be closed. if you
     * need some more after condition for your tests, please add it.
     * 
     * @throws RemoteException
     */
    @After
    public void cleanUp() throws RemoteException {
        bob.bot.resetWorkbench();
        carl.bot.resetWorkbench();
        alice.bot.resetWorkbench();
    }

    /**
     * please describe the test and change the test'name.
     * 
     * @throws RemoteException
     */
    @Test
    public void test1() throws RemoteException {

    }

    /**
     * please describe the test and change the test'name.
     * 
     * @throws RemoteException
     */
    @Test
    public void test2() throws RemoteException {

    }
}
