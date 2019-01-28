package de.fu_berlin.inf.dpp.intellij.ui.views.buttons;

import com.intellij.util.ui.UIUtil;
import javax.swing.ImageIcon;
import javax.swing.JButton;

/** Common class for Toolbar button implementations. */
abstract class ToolbarButton extends JButton {

  /** Creates a button with the specified actionCommand, Icon and toolTipText. */
  ToolbarButton(String actionCommand, String tooltipText, ImageIcon icon) {
    setActionCommand(actionCommand);
    setIcon(icon);
    setToolTipText(tooltipText);
  }

  /** calls {@link #setEnabled(boolean)} from the UI thread. */
  void setEnabledFromUIThread(final boolean enabled) {
    UIUtil.invokeAndWaitIfNeeded((Runnable) () -> setEnabled(enabled));
  }
}
