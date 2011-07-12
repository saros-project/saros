package de.fu_berlin.inf.dpp.stf.test.stf.internal;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;

public class InternalTest extends StfTestCase {

    @BeforeClass
    public static void selectTesters() throws Exception {
        select(ALICE);
    }

    @After
    public void deleteWorkspace() throws RemoteException {
        ALICE.superBot().internal().clearWorkspace();
    }

    @Test
    public void testCreateJavaProject() throws RemoteException {
        ALICE.superBot().internal().createJavaProject("Hello");
    }

    @Test
    public void testCreateProject() throws RemoteException {
        ALICE.superBot().internal().createJavaProject("Hello");
    }

    @Test
    public void testCreateFolder() throws RemoteException {
        ALICE.superBot().internal().createProject("foo");
        ALICE.superBot().internal().createFolder("foo", "bar");
    }

    @Test(expected = RemoteException.class)
    public void testCreateFolderWithoutProject() throws RemoteException {
        ALICE.superBot().internal().createFolder("foo", "bar");
    }

    @Test
    public void testChangeSarosVersion() throws RemoteException {
        ALICE.superBot().internal().changeSarosVersion("47.11.0");
    }

    @Test
    public void testcreateFile() throws RemoteException {
        ALICE.superBot().internal().createJavaProject("Hello");
        ALICE.superBot().internal().createFile("Hello", "test.bar", "bla");
        ALICE.superBot().internal()
            .createFile("Hello", "a/b/c/d/e/f/g/abc.def", "bla");
        ALICE.superBot().internal()
            .createFile("Hello", "x/y/z/foo.bar", 100, false);
        ALICE.superBot().internal()
            .createFile("Hello", "x/y/z/foo.bar.comp", 100, true);
    }

    @AfterClass
    public static void resetSarosVersion() throws RemoteException {
        ALICE.superBot().internal().resetSarosVersion();
    }
}
