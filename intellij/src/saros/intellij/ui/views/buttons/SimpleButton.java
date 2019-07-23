package saros.intellij.ui.views.buttons;

import javax.swing.ImageIcon;
import saros.intellij.ui.actions.AbstractSarosAction;

/** Simple button used to create actions that just call {@link AbstractSarosAction#execute()}. */
public class SimpleButton extends AbstractToolbarButton {
  private AbstractSarosAction action;

  /** Creates a button that executes action.execute() when clicked. */
  public SimpleButton(AbstractSarosAction action, String tooltipText, ImageIcon icon) {
    super(action.getActionName(), tooltipText, icon);

    this.action = action;
    addActionListener(actionEvent -> SimpleButton.this.action.execute());
  }
}
