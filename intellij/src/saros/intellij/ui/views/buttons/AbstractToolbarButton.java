package saros.intellij.ui.views.buttons;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saros.intellij.ui.views.SarosMainPanelView;

/**
 * Common class for Toolbar button implementations.
 *
 * <p><b>NOTE:</b>This component and any component added here must be correctly torn down when the
 * project the components belong to is closed. See {@link SarosMainPanelView}. This class offers the
 * method {@link #dispose()} for such a purpose.
 */
abstract class AbstractToolbarButton extends JButton implements Disposable {

  // Self-adjusting references to the current IDE color
  static final Color FOREGROUND_COLOR = JBColor.foreground();
  static final Color BACKGROUND_COLOR = JBColor.background();

  protected final Project project;

  /** Creates a button with the specified actionCommand, Icon and toolTipText. */
  AbstractToolbarButton(
      @NotNull Project project,
      @Nullable String actionCommand,
      @Nullable String tooltipText,
      @Nullable ImageIcon icon) {

    this.project = project;

    Disposer.register(project, this);

    setActionCommand(actionCommand);
    setButtonIcon(icon);
    setToolTipText(tooltipText);

    setBorder(BorderFactory.createLineBorder(JBColor.border(), 1, true));
  }

  /** calls {@link #setEnabled(boolean)} from the UI thread. */
  void setEnabledFromUIThread(final boolean enabled) {
    UIUtil.invokeAndWaitIfNeeded((Runnable) () -> setEnabled(enabled));
  }

  /**
   * Method to dispose components of the button.
   *
   * <p>This method is called when the project the button belongs to is disposed. It should drop any
   * internal state that would prevent the object from being garbage collected.
   *
   * @see Disposable
   */
  @Override
  public abstract void dispose();

  /**
   * Sets the icon used when displaying the button.
   *
   * <p>Sets both the icon and disabled icon.
   *
   * @param icon the icon to use for the button
   */
  void setButtonIcon(ImageIcon icon) {
    setIcon(icon);
    setDisabledIcon(icon);
  }
}
