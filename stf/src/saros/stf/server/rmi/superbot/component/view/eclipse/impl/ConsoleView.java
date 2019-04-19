package saros.stf.server.rmi.superbot.component.view.eclipse.impl;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.allOf;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;

import java.rmi.RemoteException;
import java.util.List;
import org.apache.log4j.Logger;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.WidgetResult;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotStyledText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import saros.stf.server.StfRemoteObject;
import saros.stf.server.bot.SarosSWTBotPreferences;
import saros.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import saros.stf.server.rmi.superbot.component.view.eclipse.IConsoleView;

public final class ConsoleView extends StfRemoteObject implements IConsoleView {

  private static final Logger LOG = Logger.getLogger(ConsoleView.class);

  private static final ConsoleView INSTANCE = new ConsoleView();

  private SWTBotView view;

  public static ConsoleView getInstance() {
    return INSTANCE;
  }

  public IConsoleView setView(SWTBotView view) {
    this.view = view;
    return this;
  }

  @Override
  public String getFirstTextInConsole() throws RemoteException {
    return getCurrentConsoleTextWidget().getText();
  }

  @Override
  public boolean existTextInConsole() throws RemoteException {
    try {
      if (view.bot().styledText().getText().equals("")) return false;
      return true;
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      return false;
    }
  }

  @Override
  public void waitUntilExistsTextInConsole() throws RemoteException {
    RemoteWorkbenchBot.getInstance()
        .waitUntil(
            new DefaultCondition() {
              @Override
              public boolean test() throws Exception {
                return existTextInConsole();
              }

              @Override
              public String getFailureMessage() {
                return "the console view contains no text";
              }
            });
  }

  @Override
  public void clearCurrentConsole() throws RemoteException {

    SWTBotToolbarButton clearConsoleButton = view.toolbarButton("Clear Console");

    if (clearConsoleButton.isEnabled()) clearConsoleButton.click();
  }

  @Override
  public void waitUntilCurrentConsoleContainsText(final String text) throws RemoteException {

    view.bot()
        .waitUntil(
            new DefaultCondition() {
              @Override
              public boolean test() throws Exception {
                return getCurrentConsoleTextWidget().getText().contains(text);
              }

              @Override
              public String getFailureMessage() {
                return "the console view does not contain the text: " + text;
              }
            },
            SarosSWTBotPreferences.SAROS_DEFAULT_TIMEOUT);
  }

  @SuppressWarnings("unchecked")
  private SWTBotStyledText getCurrentConsoleTextWidget() {
    // testing showed that there is only 1 styledText but play it safe

    final List<? extends StyledText> styledTexts =
        view.bot().widgets(allOf(widgetOfType(StyledText.class)), view.getWidget());

    final StyledText widget =
        UIThreadRunnable.syncExec(
            new WidgetResult<StyledText>() {

              @Override
              public StyledText run() {
                for (StyledText text : styledTexts) {
                  if (text.isDisposed()) continue;

                  if (text.isVisible()) return text;
                }
                return null;
              }
            });

    if (widget == null)
      throw new WidgetNotFoundException("the console window does not contain an active console");

    return new SWTBotStyledText(widget);
  }
}
