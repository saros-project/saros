package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.basicWidgets;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.IRemoteBotView;

public class TestBasicWidgetsTable extends STFTest {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException,
        InterruptedException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB);
        setUpWorkbench();
        setUpSaros();
        setUpSessionWithAJavaProjectAndAClass(alice, bob);
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        resetWriteAccess(alice, bob);
    }

    @Test
    public void existsTableItemInView() throws RemoteException {
        IRemoteBotView view = alice.bot().view(VIEW_SAROS);
        view.show();

        assertTrue(view.bot().table().containsItem(OWN_PARTICIPANT_NAME));
    }

    @Test
    public void selectTableItemInView() throws RemoteException {
        IRemoteBotView view = alice.bot().view(VIEW_SAROS);
        view.show();
        view.bot().table().getTableItem(bob.getBaseJid()).select();

        assertTrue(view.toolbarButton(TB_SHARE_SCREEN_WITH_BUDDY).isEnabled());

        view.bot().table().getTableItem(OWN_PARTICIPANT_NAME).select();

        assertFalse(view.toolbarButton(TB_SHARE_SCREEN_WITH_BUDDY).isEnabled());
    }

    @Test
    public void clickContextMenuOfTableInView() throws RemoteException {
        IRemoteBotView view = alice.bot().view(VIEW_SAROS);
        view.show();
        view.bot().table().getTableItem(bob.getBaseJid())
            .contextMenu(CM_RESTRICT_TO_READ_ONLY_ACCESS).click();

        bob.superBot().views().sarosView().selectParticipant(bob.getJID())
            .waitUntilHasReadOnlyAccess();
        assertTrue(bob.superBot().views().sarosView()
            .selectParticipant(bob.getJID()).hasReadOnlyAccess());
    }

    @Test
    public void isContextMenuOfTableVisibleInView() throws RemoteException {
        IRemoteBotView view = alice.bot().view(VIEW_SAROS);
        view.show();

        assertTrue(view.bot().table().getTableItem(bob.getBaseJid())
            .contextMenu(CM_RESTRICT_TO_READ_ONLY_ACCESS).isVisible());

        assertTrue(view.bot().table().getTableItem(bob.getBaseJid())
            .contextMenu(CM_CHANGE_COLOR).isVisible());
    }

    @Test
    public void isContextMenuOfTableEnabledInView() throws RemoteException {
        IRemoteBotView view = alice.bot().view(VIEW_SAROS);
        view.show();

        assertTrue(view.bot().table().getTableItem(bob.getBaseJid())
            .contextMenu(CM_RESTRICT_TO_READ_ONLY_ACCESS).isEnabled());
        assertFalse(view.bot().table().getTableItem(bob.getBaseJid())
            .contextMenu(CM_CHANGE_COLOR).isEnabled());

    }
}
