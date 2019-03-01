package de.fu_berlin.inf.dpp.ui.wizards.pages;

import de.fu_berlin.inf.dpp.editor.annotations.SarosAnnotation;
import de.fu_berlin.inf.dpp.editor.colorstorage.UserColorID;
import de.fu_berlin.inf.dpp.ui.widgets.ColorChooser;
import de.fu_berlin.inf.dpp.ui.widgets.ColorChooser.ColorSelectionListener;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

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
