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
        respondent = new Musician(new JID(BotConfiguration.JID_ALICE),
            BotConfiguration.PASSWORD_ALICE, BotConfiguration.HOST_ALICE,
            BotConfiguration.PORT_ALICE);
        respondent.initRmi();
        respondent.openSarosViews();
        respondent.xmppConnect();
    }

    @Before
    public void configureQuestioner() throws RemoteException, NotBoundException {
        questioner = new Musician(new JID(BotConfiguration.JID_BOB),
            BotConfiguration.PASSWORD_BOB, BotConfiguration.HOST_BOB,
            BotConfiguration.PORT_BOB);
        questioner.initRmi();
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
    public void testAddAndRemoveContact() throws RemoteException {
        questioner.removeContact(respondent);
        respondent.clickButtonOnPopup("Removal of subscription", "OK");
        questioner.clickButtonOnPopup("Removal of subscription", "OK");
        assertFalse(questioner.hasContact(respondent));
        assertFalse(respondent.hasContact(questioner));

        questioner.addContact(respondent);
        respondent.ackContact(questioner);
        questioner.ackContact(respondent);
        assertTrue(questioner.hasContact(respondent));
        assertTrue(respondent.hasContact(questioner));
    }
}
