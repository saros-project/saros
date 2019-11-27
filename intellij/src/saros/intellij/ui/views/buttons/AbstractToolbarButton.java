package saros.intellij.ui.views.buttons;

import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;

/** Common class for Toolbar button implementations. */
abstract class AbstractToolbarButton extends JButton {

  // Self-adjusting references to the current IDE color
  static final Color FOREGROUND_COLOR = JBColor.foreground();
  static final Color BACKGROUND_COLOR = JBColor.background();

  /** Creates a button with the specified actionCommand, Icon and toolTipText. */
  AbstractToolbarButton(String actionCommand, String tooltipText, ImageIcon icon) {
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
