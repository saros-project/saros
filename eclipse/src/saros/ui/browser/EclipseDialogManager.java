package saros.ui.browser;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;
import saros.synchronize.UISynchronizer;
import saros.ui.ide_embedding.BrowserCreator;
import saros.ui.ide_embedding.DialogManager;
import saros.ui.ide_embedding.IBrowserDialog;
import saros.ui.pages.IBrowserPage;
import saros.ui.util.SWTUtils;

/** Eclipse side implementation of the IDialogManager interface */
public class EclipseDialogManager extends DialogManager {

  private final BrowserCreator browserCreator;

  public EclipseDialogManager(BrowserCreator browserCreator, UISynchronizer uiSynchronizer) {
    super(uiSynchronizer);
    this.browserCreator = browserCreator;
  }

  @Override
  protected IBrowserDialog createDialog(final IBrowserPage browserPage) {
    final Shell activeShell = SWTUtils.getShell();

    final Shell browserShell = new Shell(activeShell);

    browserShell.setText(browserPage.getTitle());
    browserShell.setLayout(new FillLayout());
    browserShell.setMinimumSize(640, 480);

    browserCreator.createBrowser(browserShell, SWT.NONE, browserPage);

    browserShell.addShellListener(
        new ShellAdapter() {

          @Override
          public void shellClosed(ShellEvent e) {
            removeDialogEntry(browserPage.getRelativePath());
          }
        });

    browserShell.open();
    browserShell.pack();

    return new EclipseBrowserDialog(browserShell);
  }
}
