package saros.intellij.ui.swt_browser;

import java.awt.Rectangle;
import javax.swing.JDialog;
import saros.ui.ide_embedding.IBrowserDialog;

/** IntelliJ wrapper of the Swing-specific dialog class. */
public class IntelliJBrowserDialog implements IBrowserDialog {

  private JDialog jDialog;

  public IntelliJBrowserDialog(JDialog jDialog) {
    this.jDialog = jDialog;
    centerWindowToScreen();
  }

  @Override
  public void close() {
    jDialog.dispose();
  }

  @Override
  public void reopen() {
    // TODO diplay dialog on top of other windows
    centerWindowToScreen();
  }

  private void centerWindowToScreen() {
    Rectangle screen = jDialog.getGraphicsConfiguration().getBounds();
    jDialog.setLocation(
        screen.x + (screen.width - jDialog.getWidth()) / 2,
        screen.y + (screen.height - jDialog.getHeight()) / 2);
  }
}
