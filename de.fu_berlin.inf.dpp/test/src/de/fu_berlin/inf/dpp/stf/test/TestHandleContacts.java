package de.fu_berlin.inf.dpp.stf.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.Musician;

public class TestHandleContacts {
    // bots
    protected Musician questioner;
    protected Musician respondent;

    @Before
    public void configureRespondent() throws RemoteException, NotBoundException {
        // TODO Make sure that both have each other added as contacts.

        respondent = new Musician(new JID(BotConfiguration.JID_ALICE),
            BotConfiguration.PASSWORD_ALICE, BotConfiguration.HOST_ALICE,
            BotConfiguration.PORT_ALICE);
        respondent.initRmi();
        respondent.activeMusican();

        if (respondent.isViewOpen("Welcome"))
            respondent.closeViewByTitle("Welcome");

        respondent.openSarosViews();
        respondent.xmppConnect();
    }

    @Before
    public void configureQuestioner() throws RemoteException, NotBoundException {
        questioner = new Musician(new JID(BotConfiguration.JID_BOB),
            BotConfiguration.PASSWORD_BOB, BotConfiguration.HOST_BOB,
            BotConfiguration.PORT_BOB);
        questioner.initRmi();
        questioner.activeMusican();

        if (questioner.isViewOpen("Welcome"))
            questioner.closeViewByTitle("Welcome");

        questioner.openSarosViews();
        questioner.xmppConnect();

    }

    @After
    public void cleanupRespondent() throws RemoteException {
        respondent.xmppDisconnect();
    }

    @After
    public void cleanupQuestioner() throws RemoteException {
        questioner.xmppDisconnect();
    }

    @Test
    // TODO This test fails if a contact was renamed.
    public void testAddAndRemoveContact() throws RemoteException {
        questioner.waitForConnect();
        respondent.waitForConnect();
        assertTrue(questioner.hasContact(respondent));
        assertTrue(respondent.hasContact(questioner));

        questioner.removeContact(respondent);
        questioner.sleep(750);
        assertFalse(questioner.hasContact(respondent));

        respondent.clickButtonOnPopup("Removal of subscription", "OK");
        respondent.sleep(750);
        assertFalse(respondent.hasContact(questioner));

        questioner.addContact(respondent);
        questioner.sleep(750);
        respondent.ackContact(questioner);
        questioner.ackContact(respondent);
        questioner.sleep(2000);
        assertTrue(questioner.hasContact(respondent));
        assertTrue(respondent.hasContact(questioner));
    }
}
