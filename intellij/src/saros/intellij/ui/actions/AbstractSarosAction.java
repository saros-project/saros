package saros.intellij.ui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/** Parent class for all Saros actions */
public abstract class AbstractSarosAction {
  protected static final Logger LOG = Logger.getLogger(AbstractSarosAction.class);

  private final List<ActionListener> actionListeners = new ArrayList<>();

  protected void actionPerformed() {
    for (ActionListener actionListener : actionListeners) {
      actionListener.actionPerformed(new ActionEvent(this, 0, getActionName()));
    }
  }

  public void addActionListener(ActionListener actionListener) {
    actionListeners.add(actionListener);
  }

  public abstract String getActionName();

  public abstract void execute();
}
