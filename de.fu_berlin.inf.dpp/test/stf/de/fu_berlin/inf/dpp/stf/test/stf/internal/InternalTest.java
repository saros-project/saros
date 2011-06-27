package de.fu_berlin.inf.dpp.stf.test.stf.internal;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;

import java.rmi.RemoteException;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;

public class InternalTest extends StfTestCase {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(ALICE);
        setUpWorkbench();
        setUpSaros();
    }

    @Test
    public void testcreateJavaProject() throws RemoteException {
        ALICE.superBot().internal().createProject("Hello");
        ALICE.superBot().internal()
            .createFile("Hello", "src/foo/HelloWorld.java", "HelloWorld");

        ALICE.superBot().internal().createJavaProject("Hello1");

        ALICE.superBot().internal()
            .createFile("Hello1", "src/HelloWorld.java", "HelloWorld");

        ALICE.superBot().internal()
            .createFile("Hello1", "src/HelloWorld1.java", "HelloWorld");

        ALICE.superBot().internal()
            .createFile("Hello1", "src/foo/HelloWorld.java", "HelloWorld");
        ALICE.remoteBot().sleep(60000);
    }
}
