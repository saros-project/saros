package saros.stf.server.rmi.remotebot.widget.impl;

import java.rmi.RemoteException;
import java.util.List;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotStyledText;
import saros.stf.server.StfRemoteObject;
import saros.stf.server.rmi.remotebot.widget.IRemoteBotMenu;
import saros.stf.server.rmi.remotebot.widget.IRemoteBotStyledText;

public final class RemoteBotStyledText extends StfRemoteObject implements IRemoteBotStyledText {

  private static final RemoteBotStyledText INSTANCE = new RemoteBotStyledText();

  private SWTBotStyledText widget;

  public static RemoteBotStyledText getInstance() {
    return INSTANCE;
  }

  public IRemoteBotStyledText setWidget(SWTBotStyledText styledText) {
    this.widget = styledText;
    return this;
  }

  @Override
  public IRemoteBotMenu contextMenu(String text) throws RemoteException {
    return RemoteBotMenu.getInstance().setWidget(widget.contextMenu(text));
  }

  @Override
  public String getText() throws RemoteException {

    return widget.getText();
  }

  @Override
  public String getToolTipText() throws RemoteException {
    return widget.getToolTipText();
  }

  @Override
  public String getTextOnCurrentLine() throws RemoteException {
    return widget.getTextOnCurrentLine();
  }

  @Override
  public String getSelection() throws RemoteException {
    return widget.getSelection();
  }

  @Override
  public List<String> getLines() throws RemoteException {
    return widget.getLines();
  }

  @Override
  public int getLineCount() throws RemoteException {
    return widget.getLineCount();
  }
}
