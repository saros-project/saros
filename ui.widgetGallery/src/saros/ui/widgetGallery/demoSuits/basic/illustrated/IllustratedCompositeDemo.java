package saros.ui.widgetGallery.demoSuits.basic.illustrated;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import saros.ui.util.LayoutUtils;
import saros.ui.widgetGallery.annotations.Demo;
import saros.ui.widgetGallery.demoSuits.AbstractDemo;
import saros.ui.widgets.IllustratedComposite;

@Demo
public class IllustratedCompositeDemo extends AbstractDemo {
  protected IllustratedComposite createIllustratedComposite(
      final Composite parent, int iconId, int position, String text) {
    IllustratedComposite illustratedComposite =
        new IllustratedComposite(parent, SWT.NONE | position, iconId);
    illustratedComposite.setLayout(LayoutUtils.createGridLayout(2, false, 0, 10));

    Label label = new Label(illustratedComposite, SWT.WRAP);
    label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    label.setText(text);

    Button button = new Button(illustratedComposite, SWT.PUSH);
    button.setText("Press Me...");
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

    return illustratedComposite;
  }

  @Override
  public void createDemo(Composite parent) {
    parent.setLayout(new GridLayout(2, false));

    int[] iconIds =
        new int[] {
          SWT.DEFAULT,
          SWT.ICON_CANCEL,
          SWT.ICON_ERROR,
          SWT.ICON_INFORMATION,
          SWT.ICON_QUESTION,
          SWT.ICON_SEARCH,
          SWT.ICON_WARNING,
          SWT.ICON_WORKING
        };
    int[] positions =
        new int[] {
          SWT.NONE, SWT.TOP, SWT.TOP, SWT.BOTTOM, SWT.BOTTOM, SWT.CENTER, SWT.CENTER, SWT.CENTER
        };
    String[] texts =
        new String[] {
          "SWT.BORDER\nSWT.DEFAULT\nSWT.DEFAULT",
          "SWT.BORDER\nSWT.ICON_CANCEL\nSWT.TOP",
          "SWT.BORDER\nSWT.ICON_ERROR\nSWT.TOP",
          "SWT.BORDER\nSWT.ICON_INFORMATION\nSWT.BOTTOM",
          "SWT.BORDER\nSWT.ICON_QUESTION\nSWT.BOTTOM",
          "SWT.BORDER\nSWT.ICON_SEARCH\nSWT.CENTER",
          "SWT.BORDER\nSWT.ICON_WARNING\nSWT.CENTER",
          "SWT.BORDER\nSWT.ICON_WORKING\nSWT.CENTER"
        };

    for (int i = 0; i < iconIds.length; i++) {
      Label label = new Label(parent, SWT.NONE);
      label.setText(texts[i] + ((i == iconIds.length - 1) ? "\nGridData.FILL" : ""));
      label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

      String text =
          "I'm a "
              + IllustratedComposite.class.getSimpleName()
              + " instance\ncontaining this label and\na button to the right.";
      IllustratedComposite illustratedComposite =
          createIllustratedComposite(parent, iconIds[i], positions[i], text);
      illustratedComposite.setLayoutData(
          (i == iconIds.length - 1)
              ? new GridData(SWT.FILL, SWT.FILL, true, true)
              : new GridData(SWT.FILL, SWT.CENTER, true, false));
    }
  }
}
