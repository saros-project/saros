package de.fu_berlin.inf.dpp.stf.client.tester;

import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.stf.server.rmi.controlbot.IControlBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.htmlbot.IHTMLBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.htmlbot.IHTMLWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.IRemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.ISuperBot;
import java.rmi.RemoteException;

public enum SarosTester implements AbstractTester {
  ALICE(TesterFactory.createTester("ALICE")),
  BOB(TesterFactory.createTester("BOB")),
  CARL(TesterFactory.createTester("CARL")),
  DAVE(TesterFactory.createTester("DAVE")) /*
                                            * EDNA(TesterFactory
                                            * .createTester("EDNA"))
                                            */;

  private AbstractTester tester;

  private SarosTester(AbstractTester tester) {
    this.tester = tester;
  }

  @Override
  public String getName() {
    return this.tester.getName();
  }

  @Override
  public String getBaseJid() {
    return this.tester.getBaseJid();
  }

  @Override
  public String getRqJid() {
    return this.tester.getRqJid();
  }

  @Override
  public String getDomain() {
    return this.tester.getDomain();
  }

  @Override
  public JID getJID() {
    return this.tester.getJID();
  }

  @Override
  public String getPassword() {
    return this.tester.getPassword();
  }

  @Override
  public IRemoteWorkbenchBot remoteBot() throws RemoteException {
    return this.tester.remoteBot();
  }

  @Override
  public ISuperBot superBot() throws RemoteException {
    return this.tester.superBot();
  }

  @Override
  public IControlBot controlBot() throws RemoteException {
    return this.tester.controlBot();
  }

  @Override
  public boolean usesHtmlGui() {
    return this.tester.usesHtmlGui();
  }

  @Override
  public IHTMLWorkbenchBot htmlViewBot() throws RemoteException {
    return this.tester.htmlViewBot();
  }

  @Override
  public IHTMLBot htmlBot() throws RemoteException {
    return this.tester.htmlBot();
  }
}
