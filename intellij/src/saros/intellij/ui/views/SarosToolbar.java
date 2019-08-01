package saros.intellij.ui.views;

import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import java.awt.FlowLayout;
import javax.swing.JToolBar;
import org.jetbrains.annotations.NotNull;
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
 * <p><b>NOTE:</b>This component and any component added here must be correctly torn down when the
 * project the components belong to is closed. See {@link SarosMainPanelView}.
 *
 * <p>FIXME: Replace by IDEA toolbar class.
 */
class SarosToolbar extends JToolBar {

  private static final boolean ENABLE_ADD_CONTACT =
      Boolean.getBoolean("saros.intellij.ENABLE_ADD_CONTACT");
  private static final boolean ENABLE_PREFERENCES =
      Boolean.getBoolean("saros.intellij.ENABLE_PREFERENCES");

  private final Project project;

  SarosToolbar(@NotNull Project project) {
    super("Saros IDEA toolbar");

    this.project = project;

    setLayout(new FlowLayout(FlowLayout.RIGHT));

    /*
     * For an unknown reason, the color of the toolbar does not automatically match the IDE theme
     * starting with Intellij 2019.1. This is described in more detail in issue #535.
     *
     * As a workaround, this explicitly sets the color of the component to the current IDE theme
     * colors.
     */
    setForeground(JBColor.foreground());
    setBackground(JBColor.background());

    addToolbarButtons();
  }

  private void addToolbarButtons() {

    add(new ConnectButton(project));

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

    add(new FollowButton(project));

    add(new ConsistencyButton(project));

    add(new LeaveSessionButton(project));
  }
}
