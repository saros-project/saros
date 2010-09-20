package de.fu_berlin.inf.dpp.stf.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.Musician;

public class RmiTest {
    private final static Logger log = Logger.getLogger(RmiTest.class);

    protected Musician alice;

    private String projectName = BotConfiguration.PROJECTNAME;
    private String CLS_PATH = BotConfiguration.PROJECTNAME
        + "/src/pkg/Cls.java";

    @Before
    public void configureBot() throws RemoteException, NotBoundException {
        log.trace("new Musician");
        alice = new Musician(new JID(BotConfiguration.JID_ALICE),
            BotConfiguration.PASSWORD_ALICE, BotConfiguration.HOST_ALICE,
            BotConfiguration.PORT_ALICE);
        log.trace("initBot");
        alice.initBot();
    }

    @After
    public void cleanupBot() throws RemoteException {
        alice.xmppDisconnect();
        alice.bot.closeRosterView();
        alice.bot.closeChatView();
        alice.bot.closeRemoteScreenView();
        alice.bot.closeSharedSessionView();
        if (alice.bot.isJavaProjectExist(projectName))
            alice.bot.deleteProject(projectName);
    }

    @Test
    public void TestIsFileExist() throws RemoteException {
        alice.newProjectWithClass(projectName, "pkg", "Cls");
        assertTrue(alice.bot.isFileExist(CLS_PATH));
        alice.bot.deleteFile(CLS_PATH);
        assertFalse(alice.bot.isFileExist(CLS_PATH));
    }

    @Test
    public void TestIsFileExistWithGUI() throws RemoteException {

        alice.newProjectWithClass(projectName, "pkg", "Cls");
        assertTrue(alice.bot.isFileExistedWithGUI(CLS_PATH));
        alice.bot.deleteFile(CLS_PATH);

        assertFalse(alice.bot.isFileExistedWithGUI(CLS_PATH));

    }

    @Test
    public void testViews() throws RemoteException {
        alice.bot.closeRosterView();
        assertFalse(alice.bot.isRosterViewOpen());
        alice.bot.openRosterView();
        assertTrue(alice.bot.isRosterViewOpen());
    }

    @Test
    public void test_getCurrentActiveShell() throws RemoteException {
        final String currentActiveShell = alice.bot.getCurrentActiveShell();
        assertTrue(currentActiveShell != null);
    }

    @Test
    public void test_newProjectWithClass() throws RemoteException {
        assertFalse(alice.bot.isJavaProjectExist(projectName));
        alice.newProjectWithClass(projectName, "pkg", "Cls");
        assertTrue(alice.bot.isJavaProjectExist(projectName));
        assertTrue(alice.bot.isJavaClassExist(projectName, "pkg", "Cls"));

        alice.bot.deleteProject(projectName);
        assertFalse(alice.bot.isJavaProjectExist(projectName));
    }

    @Test
    public void test_newJavaClassInProject() throws RemoteException {
        final String pkg = "pkg";
        final String className = "Cls";

        log.trace("alice.isJavaProjectExist()");
        assertFalse(alice.bot.isJavaProjectExist(projectName));
        log.trace("alice.newProjectWithClass()");

        alice.newProjectWithClass(projectName, pkg, className);
        log.trace("alice.isJavaProjectExist()");
        assertTrue(alice.bot.isJavaProjectExist(projectName));
        log.trace("alice.isJavaClassExist()");
        assertTrue(alice.bot.isJavaClassExist(projectName, pkg, className));

        log.trace("alice.isJavaClassExist()");
        final String className2 = "Cls2";
        assertFalse(alice.bot.isJavaClassExist(projectName, pkg, className2));
        log.trace("alice.newJavaClassInProject()");
        alice.newJavaClassInProject(projectName, pkg, className2);

        log.trace("alice.isJavaClassExist()");
        assertTrue(alice.bot.isJavaClassExist(projectName, pkg, className2));

        log.trace("alice.deleteProject()");
        alice.bot.deleteProject(projectName);
        log.trace("alice.isJavaProjectExist()");
        assertFalse(alice.bot.isJavaProjectExist(projectName));
    }

}
