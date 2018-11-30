package de.fu_berlin.inf.dpp.intellij.ui.views.buttons;

import de.fu_berlin.inf.dpp.intellij.ui.actions.AbstractSarosAction;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/** Simple button used to create actions that just call {@link AbstractSarosAction#execute()}. */
public class SimpleButton extends ToolbarButton {
  private AbstractSarosAction action;

  /** Creates a button that exectures action.execute() when clicked. */
  public SimpleButton(
      AbstractSarosAction action, String tooltipText, String iconPath, String altText) {
    super(action.getActionName(), tooltipText, iconPath, altText);
    this.action = action;
    addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent actionEvent) {
            SimpleButton.this.action.execute();
          }
        });
  }
}
