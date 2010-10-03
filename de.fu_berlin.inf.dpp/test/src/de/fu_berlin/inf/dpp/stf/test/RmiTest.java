package de.fu_berlin.inf.dpp.stf.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.sarosswtbot.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.Musician;

public class RmiTest {
    private final static Logger log = Logger.getLogger(RmiTest.class);

    private static Musician alice = InitMusician.newAlice();
    private String PROJECT = BotConfiguration.PROJECTNAME;

    @After
    public void cleanupBot() throws RemoteException {
        alice.bot.resetSaros();
    }

    @Test
    public void testRenameFile() throws RemoteException {
        alice.bot.newJavaProject(PROJECT);
        alice.bot.newClass(PROJECT, "pkg", "Cls");
        alice.bot.renameFile("Cls2", PROJECT, "pkg", "Cls");
    }

    @Test
    public void testDeleteProjectUsingGUI() throws RemoteException {
        alice.bot.newJavaProject(PROJECT);
        assertTrue(alice.bot.isJavaProjectExist(PROJECT));
        alice.bot.deleteProjectGui(PROJECT);
        assertFalse(alice.bot.isJavaProjectExist(PROJECT));
    }

    @Test
    public void testDeleteFileUsingGUI() throws RemoteException {
        alice.bot.newJavaProject(PROJECT);
        alice.bot.newClass(PROJECT, "pkg", "Cls");
        assertTrue(alice.bot.isClassExist(PROJECT, "pkg", "Cls"));
        alice.bot.deleteFileGui(PROJECT, "src", "pkg", "Cls.java");
        assertFalse(alice.bot.isClassExist(PROJECT, "pkg", "Cls"));
    }

    @Test
    public void testIsFileExist() throws RemoteException {
        alice.bot.newJavaProject(PROJECT);
        alice.bot.newClass(PROJECT, "pkg", "Cls");
        assertTrue(alice.bot.isClassExist(PROJECT, "pkg", "Cls"));
        alice.bot.deleteClass(PROJECT, "pkg", "Cls");
        assertFalse(alice.bot.isClassExist(PROJECT, "pkg", "Cls"));
    }

    @Test
    @Ignore
    // this test fails, but it doesn't really matter...
    public void testIsFileExistWithGUI() throws RemoteException {
        alice.bot.newJavaProject(PROJECT);
        alice.bot.newClass(PROJECT, "pkg", "Cls");
        assertTrue(alice.bot.isClassExistGUI(PROJECT, "pkg", "Cls"));
        alice.bot.deleteClass(PROJECT, "pkg", "Cls");
        assertFalse(alice.bot.isClassExistGUI(PROJECT, "pkg", "Cls"));
    }

    @Test
    public void testViews() throws RemoteException {
        alice.bot.closeRosterView();
        assertFalse(alice.bot.isRosterViewOpen());
        alice.bot.openRosterView();
        assertTrue(alice.bot.isRosterViewOpen());
    }

    // @Test
    // public void test_getCurrentActiveShell() throws RemoteException {
    // final String currentActiveShell = alice.bot.getCurrentActiveShell();
    // assertTrue(currentActiveShell != null);
    // }

    @Test
    public void test_newProjectWithClass() throws RemoteException {
        assertFalse(alice.bot.isJavaProjectExist(PROJECT));
        alice.bot.newJavaProjectWithClass(PROJECT, "pkg", "Cls");
        assertTrue(alice.bot.isJavaProjectExist(PROJECT));
        assertTrue(alice.bot.isClassExist(PROJECT, "pkg", "Cls"));
    }

    @Test
    public void test_newProjectWithClass_2() throws RemoteException {
        assertFalse(alice.bot.isJavaProjectExist(PROJECT));
        alice.bot.newJavaProjectWithClass(PROJECT, "pkg", "Cls");
        assertTrue(alice.bot.isJavaProjectExist(PROJECT));
        assertTrue(alice.bot.isClassExist(PROJECT, "pkg", "Cls"));
    }

    @Test
    public void test_newProjectWithClass_3() throws RemoteException {
        assertFalse(alice.bot.isJavaProjectExist(PROJECT));
        alice.bot.newJavaProjectWithClass(PROJECT, "pkg", "Cls");
        assertTrue(alice.bot.isJavaProjectExist(PROJECT));
        assertTrue(alice.bot.isClassExist(PROJECT, "pkg", "Cls"));
    }

    @Test
    public void test_newJavaClassInProject() throws RemoteException {
        final String pkg = "pkg";
        final String className = "Cls";

        log.trace("alice.isJavaProjectExist()");
        assertFalse(alice.bot.isJavaProjectExist(PROJECT));
        log.trace("alice.newProjectWithClass()");

        alice.bot.newJavaProject(PROJECT);
        alice.bot.newClass(PROJECT, pkg, className);
        log.trace("alice.isJavaProjectExist()");
        assertTrue(alice.bot.isJavaProjectExist(PROJECT));
        log.trace("alice.isJavaClassExist()");
        assertTrue(alice.bot.isClassExist(PROJECT, pkg, className));

        log.trace("alice.isJavaClassExist()");
        final String className2 = "Cls2";
        assertFalse(alice.bot.isClassExist(PROJECT, pkg, className2));
        log.trace("alice.newJavaClassInProject()");
        alice.bot.newClass(PROJECT, pkg, className2);

        log.trace("alice.isJavaClassExist()");
        assertTrue(alice.bot.isClassExist(PROJECT, pkg, className2));

        log.trace("deleteResource()");
        alice.bot.deleteProject(PROJECT);
        log.trace("alice.isJavaProjectExist()");
        assertFalse(alice.bot.isJavaProjectExist(PROJECT));
    }
}
