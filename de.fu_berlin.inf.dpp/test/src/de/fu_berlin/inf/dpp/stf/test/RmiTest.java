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
        alice.closeRosterView();
        alice.closeChatView();
        alice.closeRmoteScreenView();
        alice.closeSarosSessionView();
        if (alice.isJavaProjectExist(projectName))
            alice.deleteProject(projectName);
    }

    @Test
    public void testViews() {
        alice.closeRosterView();
        assertFalse(alice.isRosterViewOpen());
        alice.openRosterView();
        assertTrue(alice.isRosterViewOpen());
    }

    @Test
    public void test_getCurrentActiveShell() {
        final String currentActiveShell = alice.getCurrentActiveShell();
        assertTrue(currentActiveShell != null);
    }

    @Test
    public void test_newProjectWithClass() throws RemoteException {
        assertFalse(alice.isJavaProjectExist(projectName));
        alice.newProjectWithClass(projectName, "pkg", "Cls");
        assertTrue(alice.isJavaProjectExist(projectName));
        alice.deleteProject(projectName);
        assertFalse(alice.isJavaProjectExist(projectName));
    }

    @Test
    public void test_newJavaClassInProject() throws RemoteException {
        log.trace("alice.isJavaProjectExist()");
        assertFalse(alice.isJavaProjectExist(projectName));
        log.trace("alice.newProjectWithClass()");
        alice.newProjectWithClass(projectName, "pkg", "Cls");
        log.trace("alice.isJavaProjectExist()");
        assertTrue(alice.isJavaProjectExist(projectName));
        log.trace("alice.isJavaClassExist()");
        assertTrue(alice.isJavaClassExist(projectName, "pkg", "Cls"));

        log.trace("alice.isJavaClassExist()");
        assertFalse(alice.isJavaClassExist(projectName, "pkg", "Cls2"));
        log.trace("alice.newJavaClassInProject()");
        alice.newJavaClassInProject(projectName, "pkg", "Cls2");
        log.trace("alice.isJavaClassExist()");
        assertTrue(alice.isJavaClassExist(projectName, "pkg", "Cls2"));

        log.trace("alice.deleteProject()");
        alice.deleteProject(projectName);
        log.trace("alice.isJavaProjectExist()");
        assertFalse(alice.isJavaProjectExist(projectName));
    }

}
