package saros.ui.widgetGallery.demoSuits.basic.rounded;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import saros.ui.widgetGallery.annotations.Demo;
import saros.ui.widgetGallery.demoSuits.AbstractDemo;
import saros.ui.widgets.SimpleRoundedComposite;

@Demo
public class SimpleRoundedCompositeDemo extends AbstractDemo {
  @Override
  public void createDemo(Composite parent) {
    parent.setLayout(new GridLayout(2, false));

    /* row 1 */
    Label l1 = new Label(parent, SWT.NONE);
    l1.setText("1 column\nSWT.NONE");
    l1.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

    final Color cl1 = new Color(parent.getDisplay(), 240, 233, 255);
    SimpleRoundedComposite c1 = new SimpleRoundedComposite(parent, SWT.NONE);
    c1.setBackground(cl1);
    c1.setText("Text");
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
    l2.setText("4 columns\nSWT.BORDER");
    l2.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

    final Color cl2 = new Color(parent.getDisplay(), 233, 240, 255);
    SimpleRoundedComposite c2 = new SimpleRoundedComposite(parent, SWT.BORDER);
    c2.setBackground(cl2);
    c2.setTexts(new String[] {"Column 1", "Column 2", "Column 3", "Column 4"});
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
    l3.setText("1 column\nSWT.SEPARATOR");
    l3.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

    final Color cl3 = new Color(parent.getDisplay(), 233, 255, 241);
    SimpleRoundedComposite c3 = new SimpleRoundedComposite(parent, SWT.SEPARATOR);
    c3.setBackground(cl3);
    c3.setText("Text");
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
    l4.setText("4 columns\nSWT.SEPARATOR\nSWT.BORDER");
    l4.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

    final Color cl4 = new Color(parent.getDisplay(), 255, 255, 233);
    SimpleRoundedComposite c4 = new SimpleRoundedComposite(parent, SWT.SEPARATOR | SWT.BORDER);
    c4.setBackground(cl4);
    c4.setTexts(new String[] {"Column 1", "Column 2", "Column 3", "Column 4"});
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
    l5.setText("4 columns\nSWT.NONE\nGrid Fill");
    l5.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

    final Color cl5 = new Color(parent.getDisplay(), 233, 245, 255);
    SimpleRoundedComposite c5 = new SimpleRoundedComposite(parent, SWT.NONE);
    c5.setBackground(cl5);
    c5.setTexts(new String[] {"Column 1", "Column 2", "Column 3", "Column 4"});
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
    l6.setText("4 columns\nSWT.SEPARATOR\nSWT.BORDER\nGrid Fill");
    l6.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

    final Color cl6 = new Color(parent.getDisplay(), 255, 230, 230);
    SimpleRoundedComposite c6 = new SimpleRoundedComposite(parent, SWT.SEPARATOR | SWT.BORDER);
    c6.setBackground(cl6);
    c6.setTexts(new String[] {"Column 1", "Column 2", "Column 3", "Column 4"});
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
