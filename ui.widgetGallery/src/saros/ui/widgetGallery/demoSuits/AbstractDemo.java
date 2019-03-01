package saros.ui.widgetGallery.demoSuits;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import saros.ui.widgetGallery.util.CompositeUtils;
import saros.ui.widgets.decoration.EmptyText;

public abstract class AbstractDemo {

  /** Contains all controls and the content. */
  protected Composite composite;

  protected Composite content;

  /** Contains the console for debug information send by the {@link AbstractDemo} instance. */
  protected EmptyText console;

  protected static final SimpleDateFormat consoleDateFormat = new SimpleDateFormat("HH:mm:ss");

  protected int consoleHeight = 150;

  public void createPartControls(Composite composite) {
    this.composite = composite;
    this.composite.setLayout(GridLayoutFactory.fillDefaults().spacing(0, 0).create());

    this.content = new Composite(this.composite, SWT.NONE);
    this.content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    Text console = new Text(this.composite, SWT.V_SCROLL | SWT.BORDER | SWT.MULTI);
    console.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    this.console = new EmptyText(console, "Debug Console");
    this.hideConsole();

    recreateDemo();
  }

  /** Shows the console */
  public void showConsole() {
    ((GridData) this.console.getControl().getLayoutData()).heightHint = consoleHeight;
    this.console.getControl().setVisible(true);
    this.composite.layout();
  }

  /** Hides the console */
  public void hideConsole() {
    ((GridData) this.console.getControl().getLayoutData()).heightHint = 0;
    this.console.getControl().setVisible(false);
    this.composite.layout();
  }

  /**
   * Adds a message to the console and shows it if hidden.
   *
   * @param message
   */
  public void addConsoleMessage(String message) {
    final String newLine = consoleDateFormat.format(new Date()) + " " + message + "\n";
    String oldText = console.getText();
    String newText = oldText + newLine;
    console.setText(newText);
    console.getControl().setSelection(newText.length());
    this.showConsole();
  }

  /**
   * Creates the content for this demo
   *
   * @param composite
   */
  public abstract void createDemo(Composite composite);

  public void dispose() {
    // Do nothing by default
  }

  /** Recreates the demo. Especially useful if debug mode is enabled. */
  public void recreateDemo() {
    CompositeUtils.emptyComposite(this.content);
    this.content.setLayout(new FillLayout());
    createDemo(this.content);
    this.content.layout();
  }
}
