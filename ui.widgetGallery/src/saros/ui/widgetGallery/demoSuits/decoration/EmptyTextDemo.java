package saros.ui.widgetGallery.demoSuits.decoration;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import saros.ui.widgetGallery.annotations.Demo;
import saros.ui.widgetGallery.demoSuits.AbstractDemo;
import saros.ui.widgets.decoration.EmptyText;

@Demo
public class EmptyTextDemo extends AbstractDemo {

  private Text createTextControl(final Composite parent, String emptyText, boolean multiline) {

    Text textControl = new Text(parent, SWT.BORDER | ((multiline) ? SWT.MULTI : SWT.SINGLE));
    new EmptyText(textControl, emptyText);

    return textControl;
  }

  private Label createLabel(Composite parent, String text) {
    Label label = new Label(parent, SWT.NONE);
    label.setText("Default text: " + text);
    label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

    return label;
  }

  private Text createTextEntryField(
      Composite parent,
      String defaultText,
      boolean multiline,
      int horizontalAlignment,
      int verticalAlignment,
      boolean grabExcessHorizontalSpace,
      boolean grabExcessVerticalSpace) {

    Text textControl = createTextControl(parent, defaultText, multiline);
    textControl.setLayoutData(
        new GridData(
            horizontalAlignment,
            verticalAlignment,
            grabExcessHorizontalSpace,
            grabExcessVerticalSpace));

    return textControl;
  }

  @Override
  public void createDemo(Composite parent) {
    parent.setLayout(new GridLayout(2, false));

    createLabel(parent, "Optional");
    createTextEntryField(parent, "Optional", false, SWT.FILL, SWT.CENTER, true, false);

    createLabel(parent, "Please type here ... ");
    createTextEntryField(parent, "Please type here ... ", false, SWT.FILL, SWT.CENTER, true, false);

    createLabel(parent, "Multline Text\n...\n...\n...\nGridData.FILL");
    createTextEntryField(
        parent, "Multline Text\n...\n...\n...", true, SWT.FILL, SWT.FILL, true, true);
  }
}
