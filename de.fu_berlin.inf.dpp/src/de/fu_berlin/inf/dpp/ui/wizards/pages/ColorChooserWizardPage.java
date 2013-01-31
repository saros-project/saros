package de.fu_berlin.inf.dpp.ui.wizards.pages;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.project.internal.SarosSession;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.widgets.ColorChooser;
import de.fu_berlin.inf.dpp.ui.widgets.ColorChooser.ColorSelectionListener;

public class ColorChooserWizardPage extends WizardPage {

    private ColorChooser colorChooser;
    private int selectedColor = -1;

    public ColorChooserWizardPage() {
        super(ColorChooserWizardPage.class.getName());
        setTitle(Messages.ChangeColorWizardPage_title);
        setDescription(Messages.ChangeColorWizardPage_description);
    }

    @Override
    public void createControl(Composite parent) {
        setPageComplete(selectedColor != -1);

        colorChooser = new ColorChooser(parent, SWT.NONE);
        colorChooser.updateColorEnablement();

        ColorSelectionListener listener = new ColorSelectionListener() {
            @Override
            public void selectionChanged(int colorId) {
                setPageComplete(colorId >= 0
                    && colorId < SarosSession.MAX_USERCOLORS);
                selectedColor = colorId;
            }
        };

        colorChooser.addSelectionListener(listener);

        if (selectedColor != -1)
            colorChooser.selectColor(selectedColor);

        setControl(colorChooser);
    }

    public int getSelectedColor() {
        return selectedColor;
    }
}
