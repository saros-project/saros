package de.fu_berlin.inf.dpp.ui.wizards.pages;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.editor.colorstorage.UserColorID;
import de.fu_berlin.inf.dpp.project.internal.SarosSession;
import de.fu_berlin.inf.dpp.ui.widgets.ColorChooser;
import de.fu_berlin.inf.dpp.ui.widgets.ColorChooser.ColorSelectionListener;

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

        colorChooser = new ColorChooser(parent, SWT.NONE);

        if (hideUnavailableColors)
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

        if (selectedColor != UserColorID.UNKNOWN)
            colorChooser.selectColor(selectedColor);

        setControl(colorChooser);
    }

    public int getSelectedColor() {
        return selectedColor;
    }
}
