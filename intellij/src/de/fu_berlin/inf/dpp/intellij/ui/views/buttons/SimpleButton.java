package de.fu_berlin.inf.dpp.intellij.ui.views.buttons;

import de.fu_berlin.inf.dpp.intellij.ui.actions.AbstractSarosAction;
import javax.swing.ImageIcon;

/** Simple button used to create actions that just call {@link AbstractSarosAction#execute()}. */
public class SimpleButton extends ToolbarButton {
  private AbstractSarosAction action;

  /** Creates a button that executes action.execute() when clicked. */
  public SimpleButton(AbstractSarosAction action, String tooltipText, ImageIcon icon) {
    super(action.getActionName(), tooltipText, icon);

    this.action = action;
    addActionListener(actionEvent -> SimpleButton.this.action.execute());
  }
}
