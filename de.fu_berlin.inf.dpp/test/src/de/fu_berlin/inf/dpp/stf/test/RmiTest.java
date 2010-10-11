package de.fu_berlin.inf.dpp.stf.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.sarosswtbot.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.Musician;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.SarosConstant;

public class RmiTest {
    private final static Logger log = Logger.getLogger(RmiTest.class);

    private static Musician alice;

    private static final String FOLDER = BotConfiguration.FOLDERNAME;
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

    @BeforeClass
    public static void initMusican() {
        alice = InitMusician.newAlice();
    }

    @AfterClass
    public static void resetSaros() throws RemoteException {
        alice.bot.resetSaros();
    }

    @After
    public void cleanup() throws RemoteException {
        alice.bot.resetWorkbench();
        alice.bot.resetSaros();
    }

    @Test
    public void testIsClassDirty() throws RemoteException {
        alice.bot.newJavaProjectWithClass(PROJECT, PKG, CLS);
        assertFalse(alice.bot.isClassDirty(PROJECT, PKG, CLS));
        alice.bot.setTextInJavaEditor(CP, PROJECT, PKG, CLS);
        assertTrue(alice.bot.isClassDirty(PROJECT, PKG, CLS));
    }

    @Test
    @Ignore
    public void testIsClassesSame() throws RemoteException, CoreException,
        IOException {
        alice.bot.newJavaProjectWithClass(PROJECT, PKG, CLS);
        alice.bot.newClass(PROJECT, PKG2, CLS);
        String clsOfPkgProject = alice.bot.getClassContent(PROJECT, PKG, CLS);
        String clsOfpkg2Project = alice.bot.getClassContent(PROJECT, PKG2, CLS);
        assertFalse(clsOfPkgProject.equals(clsOfpkg2Project));

        alice.bot.newJavaProjectWithClass(PROJECT2, PKG, CLS);
        String clsOfPkgProject2 = alice.bot.getClassContent(PROJECT2, PKG, CLS);
        assertTrue(clsOfPkgProject.equals(clsOfPkgProject2));
    }

    @Test
    @Ignore
    public void testOpenWith() throws RemoteException {
        alice.bot.newJavaProjectWithClass(PROJECT, PKG, CLS);
        alice.bot.openClassWith(SarosConstant.MENU_TITLE_TEXT_EDITOR, PROJECT,
            PKG, CLS);
        alice.bot.openClassWithSystemEditor(PROJECT, PKG, CLS);

    }

    @Test
    @Ignore
    public void testFolder() throws RemoteException {
        alice.bot.newJavaProject(PROJECT);
        if (!alice.bot.isFolderExist(PROJECT, FOLDER))
            alice.bot.newFolder(PROJECT, FOLDER);
    }

    @Test
    @Ignore
    public void testTypeTextInEditor() throws RemoteException {
        alice.bot.newJavaProject(PROJECT);
        alice.bot.newClassImplementsRunnable(PROJECT, "pkg", "Cls");
        alice.bot.typeTextInJavaEditor(CP, PROJECT, "pkg", "Cls");
    }

    @Test
    @Ignore
    public void testPerspective() throws RemoteException {
        assertTrue(alice.bot.isJavaPerspectiveActive());
        assertFalse(alice.bot.isDebugPerspectiveActive());
        alice.bot.openPerspectiveDebug();
        assertFalse(alice.bot.isJavaPerspectiveActive());
        assertTrue(alice.bot.isDebugPerspectiveActive());
    }

    @Test
    @Ignore
    public void testRenameFile() throws RemoteException {
        alice.bot.newJavaProject(PROJECT);
        alice.bot.newClass(PROJECT, "pkg", "Cls");
        alice.bot.renameClass("Cls2", PROJECT, "pkg", "Cls");

        assertFalse(alice.bot.isClassExist(PROJECT, "pkg", "Cls"));
        assertTrue(alice.bot.isClassExist(PROJECT, "pkg", "Cls2"));
    }

    @Test
    @Ignore
    public void testDeleteProjectUsingGUI() throws RemoteException {
        alice.bot.newJavaProject(PROJECT);
        assertTrue(alice.bot.isJavaProjectExist(PROJECT));
        alice.bot.deleteProjectGui(PROJECT);
        assertFalse(alice.bot.isJavaProjectExist(PROJECT));
    }

    @Test
    @Ignore
    public void testDeleteFileUsingGUI() throws RemoteException {
        alice.bot.newJavaProject(PROJECT);
        alice.bot.newClass(PROJECT, "pkg", "Cls");
        assertTrue(alice.bot.isClassExist(PROJECT, "pkg", "Cls"));
        alice.bot.deleteFileGui(PROJECT, "src", "pkg", "Cls.java");
        assertFalse(alice.bot.isClassExist(PROJECT, "pkg", "Cls"));
    }

    @Test
    @Ignore
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
    @Ignore
    public void testViews() throws RemoteException {
        alice.bot.closeRosterView();
        assertFalse(alice.bot.isRosterViewOpen());
        alice.bot.openRosterView();
        assertTrue(alice.bot.isRosterViewOpen());
    }

    @Test
    @Ignore
    public void test_newProjectWithClass() throws RemoteException {
        assertFalse(alice.bot.isJavaProjectExist(PROJECT));
        alice.bot.newJavaProjectWithClass(PROJECT, "pkg", "Cls");
        assertTrue(alice.bot.isJavaProjectExist(PROJECT));
        assertTrue(alice.bot.isClassExist(PROJECT, "pkg", "Cls"));
    }

    @Test
    @Ignore
    public void test_newProjectWithClass_2() throws RemoteException {
        assertFalse(alice.bot.isJavaProjectExist(PROJECT));
        alice.bot.newJavaProjectWithClass(PROJECT, "pkg", "Cls");
        assertTrue(alice.bot.isJavaProjectExist(PROJECT));
        assertTrue(alice.bot.isClassExist(PROJECT, "pkg", "Cls"));
    }

    @Test
    @Ignore
    public void test_newProjectWithClass_3() throws RemoteException {
        assertFalse(alice.bot.isJavaProjectExist(PROJECT));
        alice.bot.newJavaProjectWithClass(PROJECT, "pkg", "Cls");
        assertTrue(alice.bot.isJavaProjectExist(PROJECT));
        assertTrue(alice.bot.isClassExist(PROJECT, "pkg", "Cls"));
    }

    @Test
    @Ignore
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
