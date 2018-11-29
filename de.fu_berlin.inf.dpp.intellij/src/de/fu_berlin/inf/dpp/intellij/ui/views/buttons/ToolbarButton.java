package de.fu_berlin.inf.dpp.intellij.ui.views.buttons;

import com.intellij.util.ui.UIUtil;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import org.apache.log4j.Logger;

/** Common class for Toolbar button implementations. */
public abstract class ToolbarButton extends JButton {

  private static final Logger LOG = Logger.getLogger(ToolbarButton.class);

  /** Creates a button with the specified actionCommand, Icon and toolTipText. */
  protected ToolbarButton(
      String actionCommand, String tooltipText, String iconPath, String altText) {
    setActionCommand(actionCommand);
    setIcon(iconPath, altText);
    setToolTipText(tooltipText);
  }

  /**
   * Tries to load the icon from the specified path and sets only the altText if the loading fails.
   */
  protected void setIcon(String path, String altText) {
    URL imageURL = ToolbarButton.class.getClassLoader().getResource(path);
    if (imageURL != null) {
      setIcon(new ImageIcon(imageURL, altText));
    } else {
      setText(altText);
      LOG.error("Resource not found: " + path);
    }
  }

  /** calls {@link #setEnabled(boolean)} from the UI thread. */
  protected void setEnabledFromUIThread(final boolean enabled) {
    UIUtil.invokeAndWaitIfNeeded(
        new Runnable() {
          @Override
          public void run() {
            setEnabled(enabled);
          }
        });
  }
}
