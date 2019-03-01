package saros.ui.browser;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import saros.ui.ide_embedding.IBrowserDialog;

/** Implements the Eclipse wrapper for the SWT-specific dialog shell. */
public class EclipseBrowserDialog implements IBrowserDialog {

  private final Shell shell;

  public EclipseBrowserDialog(Shell shell) {
    this.shell = shell;
    centerShellRelativeToParent();
  }

  @Override
  public void close() {
    shell.close();
  }

  @Override
  public void reopen() {
    centerShellRelativeToParent();
    shell.setActive();
    shell.open();
  }

  private void centerShellRelativeToParent() {
    final Composite composite = shell.getParent();

    if (!(composite instanceof Shell)) return;

    final Shell parent = (Shell) composite;

    final Rectangle parentShellBounds = parent.getBounds();
    final Point shellSize = shell.getSize();

    shell.setLocation(
        parentShellBounds.x + (parentShellBounds.width - shellSize.x) / 2,
        parentShellBounds.y + (parentShellBounds.height - shellSize.y) / 2);
  }
}
