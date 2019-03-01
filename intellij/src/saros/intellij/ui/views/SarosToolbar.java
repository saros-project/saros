package saros.intellij.ui.views;

import java.awt.FlowLayout;
import javax.swing.JToolBar;
import saros.intellij.ui.actions.NotImplementedAction;
import saros.intellij.ui.util.IconManager;
import saros.intellij.ui.views.buttons.ConnectButton;
import saros.intellij.ui.views.buttons.ConsistencyButton;
import saros.intellij.ui.views.buttons.FollowButton;
import saros.intellij.ui.views.buttons.LeaveSessionButton;
import saros.intellij.ui.views.buttons.SimpleButton;

/**
 * Saros toolbar. Displays several buttons for interacting with Saros.
 *
 * <p>FIXME: Replace by IDEA toolbar class.
 */
class SarosToolbar extends JToolBar {

  private static final boolean ENABLE_ADD_CONTACT =
      Boolean.getBoolean("saros.intellij.ENABLE_ADD_CONTACT");
  private static final boolean ENABLE_PREFERENCES =
      Boolean.getBoolean("saros.intellij.ENABLE_PREFERENCES");

  SarosToolbar() {
    super("Saros IDEA toolbar");
    setLayout(new FlowLayout(FlowLayout.RIGHT));
    addToolbarButtons();
  }

  private void addToolbarButtons() {

    add(new ConnectButton());

    if (ENABLE_ADD_CONTACT) {
      add(
          new SimpleButton(
              new NotImplementedAction("addContact"),
              "Add contact to list",
              IconManager.ADD_CONTACT_ICON));
    }

    if (ENABLE_PREFERENCES) {
      add(
          new SimpleButton(
              new NotImplementedAction("preferences"),
              "Open preferences",
              IconManager.OPEN_PREFERENCES_ICON));
    }

    add(new FollowButton());

    add(new ConsistencyButton());

    add(new LeaveSessionButton());
  }
}
