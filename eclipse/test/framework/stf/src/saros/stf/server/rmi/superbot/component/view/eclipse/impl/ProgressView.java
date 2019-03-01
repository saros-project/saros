package saros.stf.server.rmi.superbot.component.view.eclipse.impl;

import java.rmi.RemoteException;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import saros.stf.server.StfRemoteObject;
import saros.stf.server.rmi.superbot.component.view.eclipse.IProgressView;

public final class ProgressView extends StfRemoteObject implements IProgressView {

  private static final ProgressView INSTANCE = new ProgressView();

  private SWTBotView view;

  public static ProgressView getInstance() {
    return INSTANCE;
  }

  public IProgressView setView(SWTBotView view) {
    this.view = view;

    return this;
  }

  @Override
  public void removeProgress() throws RemoteException {
    view.bot().toolbarButton().click();
  }

  @Override
  public void removeProcess(int index) throws RemoteException {
    view.toolbarButton(TB_REMOVE_ALL_FINISHED_OPERATIONS).click();
    view.bot().toolbarButton(index).click();
  }
}
