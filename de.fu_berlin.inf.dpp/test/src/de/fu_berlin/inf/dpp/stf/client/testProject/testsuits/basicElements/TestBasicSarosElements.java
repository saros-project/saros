package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.basicElements;

import static org.junit.Assert.assertEquals;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.helpers.STFTest;

public class TestBasicSarosElements extends STFTest {
    private static final Logger log = Logger
        .getLogger(TestBasicSarosElements.class);

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE);
        setUpWorkbenchs();
    }

    @AfterClass
    public static void runAfterClass() {
        //
    }

    @Before
    public void runBeforeEveryTest() {
        //
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        alice.rosterV.disconnectGUI();
    }

    @Test
    public void testSessionView() throws RemoteException {
        alice.sessionV.closeSessionView();
        assertEquals(false, alice.sessionV.isSessionViewOpen());
        alice.sessionV.openSessionView();
        alice.workbench.captureScreenshot((alice.workbench
            .getPathToScreenShot() + "/session_view.png"));
        assertEquals(true, alice.sessionV.isSessionViewOpen());
    }

    @Test
    public void testRosterView() throws RemoteException {
        alice.rosterV.closeRosterView();
        assertEquals(false, alice.rosterV.isRosterViewOpen());
        alice.rosterV.openRosterView();
        alice.workbench.captureScreenshot((alice.workbench
            .getPathToScreenShot() + "/roster_view.png"));
        assertEquals(true, alice.rosterV.isRosterViewOpen());
    }

    @Test
    public void testChatView() throws RemoteException {
        alice.chatV.closeChatView();
        assertEquals(false, alice.chatV.isChatViewOpen());
        alice.chatV.openChatView();
        assertEquals(true, alice.chatV.isChatViewOpen());
    }

    @Test
    public void testRemoteScreenView() throws RemoteException {
        alice.rSV.closeRemoteScreenView();
        assertEquals(false, alice.rSV.isRemoteScreenViewOpen());
        alice.rSV.openRemoteScreenView();
        assertEquals(true, alice.rSV.isRemoteScreenViewOpen());
    }

    @Test
    public void connectWithoutGUI() throws RemoteException {
        log.trace("xmppConnect");
        alice.rosterV.connect(alice.jid, alice.password);
        log.trace("captureScreenshot");
        alice.workbench.captureScreenshot((alice.workbench
            .getPathToScreenShot() + "/xmpp_connected.png"));
        assertEquals(true, alice.rosterV.isConnected());
    }

    @Test
    public void disconnectWithoutGUI() throws RemoteException {
        alice.rosterV.connect(alice.jid, alice.password);
        alice.rosterV.disconnect();
        assertEquals(false, alice.rosterV.isConnected());
    }

    @Test
    public void disconnectGUI() throws RemoteException {
        alice.rosterV.connect(alice.jid, alice.password);
        alice.rosterV.disconnectGUI();
        alice.workbench.captureScreenshot((alice.workbench
            .getPathToScreenShot() + "/xmpp_disconnected.png"));
        assertEquals(false, alice.rosterV.isConnected());
    }

    @Test
    public void testTypeInEditor() throws RemoteException {
        alice.pEV.newProject(PROJECT1);
        String fileName = "test.txt";
        String[] path = { PROJECT1, fileName };
        alice.pEV.newFile(path);
        alice.editor.waitUntilEditorActive(fileName);

        String expected = "Hello World";
        alice.editor.typeTextInEditor(expected, path);
        assertEquals(expected, alice.editor.getTextOfEditor(path));
    }

    @Test
    public void testDeleteInEditor() throws RemoteException {
        alice.pEV.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
        alice.pEV.openClass(PROJECT1, PKG1, CLS1);
        alice.editor.waitUntilJavaEditorActive(CLS1);
        String fileName = CLS1 + ".java";
        alice.editor.navigateInEditor(fileName, 3, 0);
        alice.editor.typeTextInJavaEditor("testtext", PROJECT1, PKG1, CLS1);
        alice.editor.navigateInEditor(fileName, 3, 3);
        alice.editor.pressShortcutInEditor(fileName, "DELETE", "DELETE");
        assertEquals("tesext",
            alice.editor.getJavaTextOnLine(PROJECT1, PKG1, CLS1, 3));
    }
}
