package saros.stf.client.tester;

import java.rmi.RemoteException;
import saros.net.xmpp.JID;
import saros.stf.server.rmi.controlbot.IControlBot;
import saros.stf.server.rmi.htmlbot.IHTMLBot;
import saros.stf.server.rmi.htmlbot.IHTMLWorkbenchBot;
import saros.stf.server.rmi.remotebot.IRemoteWorkbenchBot;
import saros.stf.server.rmi.superbot.ISuperBot;

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
