package saros.ui.wizards.pages;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import saros.editor.annotations.SarosAnnotation;
import saros.editor.colorstorage.UserColorID;
import saros.ui.widgets.ColorChooser;
import saros.ui.widgets.ColorChooser.ColorSelectionListener;

public class ColorChooserWizardPage extends WizardPage {

  private ColorChooser colorChooser;
  private int selectedColor = UserColorID.UNKNOWN;
  private boolean hideUnavailableColors;

  public ColorChooserWizardPage(boolean hideUnavailableColors) {
    super(ColorChooserWizardPage.class.getName());
    this.hideUnavailableColors = hideUnavailableColors;
  }

  @Override
  public void createControl(Composite parent) {
    setPageComplete(selectedColor != UserColorID.UNKNOWN);

    Composite composite = new Composite(parent, SWT.NONE);
    composite.setLayout(new GridLayout());

    colorChooser = new ColorChooser(composite, SWT.NONE);

    colorChooser.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));

    if (hideUnavailableColors) colorChooser.updateColorEnablement();

    ColorSelectionListener listener =
        new ColorSelectionListener() {
          @Override
          public void selectionChanged(int colorId) {
            setPageComplete(colorId >= 0 && colorId < SarosAnnotation.SIZE);
            selectedColor = colorId;
          }
        };

    colorChooser.addSelectionListener(listener);

    if (selectedColor != UserColorID.UNKNOWN) colorChooser.selectColor(selectedColor);

    setControl(composite);
  }

  public int getSelectedColor() {
    return selectedColor;
  }
}
