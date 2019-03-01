package saros.ui.widgetGallery.demoSuits.basic.rounded;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import saros.ui.widgetGallery.annotations.Demo;
import saros.ui.widgetGallery.demoSuits.AbstractDemo;
import saros.ui.widgets.RoundedComposite;

@Demo
public class RoundedCompositeDemo extends AbstractDemo {
  public void fillComposite(final Composite parent) {
    parent.setLayout(new GridLayout(2, false));

    Label label = new Label(parent, SWT.NONE);
    label.setText("I'm a label.");

    Button button = new Button(parent, SWT.PUSH);
    button.setText("I'm a push button");
    button.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            MessageBox dialog =
                new MessageBox(parent.getShell(), SWT.PRIMARY_MODAL | SWT.OK | SWT.ICON_WORKING);
            dialog.setText("Ouch...");
            dialog.setMessage("You hit me!");
            dialog.open();
          }
        });
  }

  @Override
  public void createDemo(Composite parent) {
    parent.setLayout(new GridLayout(2, false));

    /* row 1 */
    Label l1 = new Label(parent, SWT.NONE);
    l1.setText("SWT.NONE");
    l1.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

    final Color cl1 = new Color(parent.getDisplay(), 240, 233, 255);
    RoundedComposite c1 = new RoundedComposite(parent, SWT.NONE);
    c1.setBackground(cl1);
    fillComposite(c1);
    c1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    c1.addDisposeListener(
        new DisposeListener() {
          @Override
          public void widgetDisposed(DisposeEvent e) {
            cl1.dispose();
          }
        });

    /* row 2 */
    Label l2 = new Label(parent, SWT.NONE);
    l2.setText("SWT.BORDER");
    l2.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

    final Color cl2 = new Color(parent.getDisplay(), 233, 240, 255);
    RoundedComposite c2 = new RoundedComposite(parent, SWT.BORDER);
    c2.setBackground(cl2);
    fillComposite(c2);
    c2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    c2.addDisposeListener(
        new DisposeListener() {
          @Override
          public void widgetDisposed(DisposeEvent e) {
            cl2.dispose();
          }
        });

    /* row 3 */
    Label l3 = new Label(parent, SWT.NONE);
    l3.setText("SWT.SEPARATOR");
    l3.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

    final Color cl3 = new Color(parent.getDisplay(), 233, 255, 241);
    RoundedComposite c3 = new RoundedComposite(parent, SWT.SEPARATOR);
    c3.setBackground(cl3);
    fillComposite(c3);
    c3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    c3.addDisposeListener(
        new DisposeListener() {
          @Override
          public void widgetDisposed(DisposeEvent e) {
            cl3.dispose();
          }
        });

    /* row 4 */
    Label l4 = new Label(parent, SWT.NONE);
    l4.setText("SWT.SEPARATOR\nSWT.BORDER");
    l4.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

    final Color cl4 = new Color(parent.getDisplay(), 255, 255, 233);
    RoundedComposite c4 = new RoundedComposite(parent, SWT.SEPARATOR | SWT.BORDER);
    c4.setBackground(cl4);
    fillComposite(c4);
    c4.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    c4.addDisposeListener(
        new DisposeListener() {
          @Override
          public void widgetDisposed(DisposeEvent e) {
            cl4.dispose();
          }
        });

    /* row 5 */
    Label l5 = new Label(parent, SWT.NONE);
    l5.setText("SWT.NONE\nGrid Fill");
    l5.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

    final Color cl5 = new Color(parent.getDisplay(), 233, 245, 255);
    RoundedComposite c5 = new RoundedComposite(parent, SWT.NONE);
    c5.setBackground(cl5);
    fillComposite(c5);
    c5.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    c5.addDisposeListener(
        new DisposeListener() {
          @Override
          public void widgetDisposed(DisposeEvent e) {
            cl5.dispose();
          }
        });

    /* row 6 */
    Label l6 = new Label(parent, SWT.NONE);
    l6.setText("SWT.SEPARATOR\nSWT.BORDER\nGrid Fill");
    l6.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

    final Color cl6 = new Color(parent.getDisplay(), 255, 230, 230);
    RoundedComposite c6 = new RoundedComposite(parent, SWT.SEPARATOR | SWT.BORDER);
    c6.setBackground(cl6);
    fillComposite(c6);
    c6.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    c6.addDisposeListener(
        new DisposeListener() {
          @Override
          public void widgetDisposed(DisposeEvent e) {
            cl5.dispose();
          }
        });
  }
}
