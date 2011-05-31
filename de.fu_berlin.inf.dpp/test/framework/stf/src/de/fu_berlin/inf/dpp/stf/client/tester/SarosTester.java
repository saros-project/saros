package de.fu_berlin.inf.dpp.stf.client.tester;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.IRemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.ISuperBot;

public enum SarosTester implements AbstractTester {
    ALICE(TesterFactory.createTester("ALICE")), BOB(TesterFactory
        .createTester("BOB")), CARL(TesterFactory.createTester("CARL")), DAVE(
        TesterFactory.createTester("DAVE")), EDNA(TesterFactory
        .createTester("EDNA"));

    private AbstractTester tester;

    private SarosTester(AbstractTester tester) {
        this.tester = tester;
    }

    public String getName() {
        return this.tester.getName();
    }

    public String getBaseJid() {
        return this.tester.getBaseJid();
    }

    public String getRqJid() {
        return this.tester.getRqJid();
    }

    public String getDomain() {
        return this.tester.getDomain();
    }

    public JID getJID() {
        return this.tester.getJID();
    }

    public String getPassword() {
        return this.tester.getPassword();
    }

    public IRemoteWorkbenchBot remoteBot() {
        return this.tester.remoteBot();
    }

    public ISuperBot superBot() throws RemoteException {
        return this.tester.superBot();
    }
}