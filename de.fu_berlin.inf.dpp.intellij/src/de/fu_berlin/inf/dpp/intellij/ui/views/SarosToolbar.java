package de.fu_berlin.inf.dpp.intellij.ui.views;

import de.fu_berlin.inf.dpp.intellij.ui.actions.NotImplementedAction;
import de.fu_berlin.inf.dpp.intellij.ui.views.buttons.ConnectButton;
import de.fu_berlin.inf.dpp.intellij.ui.views.buttons.ConsistencyButton;
import de.fu_berlin.inf.dpp.intellij.ui.views.buttons.FollowButton;
import de.fu_berlin.inf.dpp.intellij.ui.views.buttons.LeaveSessionButton;
import de.fu_berlin.inf.dpp.intellij.ui.views.buttons.SimpleButton;
import java.awt.FlowLayout;
import javax.swing.JToolBar;

/**
 * Saros toolbar. Displays several buttons for interacting with Saros.
 *
 * <p>FIXME: Replace by IDEA toolbar class.
 */
public class SarosToolbar extends JToolBar {
  public static final String ADD_CONTACT_ICON_PATH = "/icons/famfamfam/contact_add_tsk.png";
  public static final String OPEN_REFS_ICON_PATH = "/icons/famfamfam/test_con.gif";

  private static final boolean ENABLE_FOLLOW_MODE =
      Boolean.getBoolean("saros.intellij.ENABLE_FOLLOW_MODE");
  private static final boolean ENABLE_ADD_CONTACT =
      Boolean.getBoolean("saros.intellij.ENABLE_ADD_CONTACT");
  private static final boolean ENABLE_PREFERENCES =
      Boolean.getBoolean("saros.intellij.ENABLE_PREFERENCES");

  public SarosToolbar() {
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
              ADD_CONTACT_ICON_PATH,
              "addContact"));
    }

    if (ENABLE_PREFERENCES) {
      add(
          new SimpleButton(
              new NotImplementedAction("preferences"),
              "Open preferences",
              OPEN_REFS_ICON_PATH,
              "preferences"));
    }

    if (ENABLE_FOLLOW_MODE) {
      add(new FollowButton());
    }

    add(new ConsistencyButton());

    add(new LeaveSessionButton());
  }
}
