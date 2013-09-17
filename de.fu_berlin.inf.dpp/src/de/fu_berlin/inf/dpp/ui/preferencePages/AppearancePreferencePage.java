package de.fu_berlin.inf.dpp.ui.preferencePages;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.widgets.ColorChooser;
import de.fu_berlin.inf.dpp.ui.widgets.ColorChooser.ColorSelectionListener;

/**
 * This class is responsible for allowing the user to select his / her favorite
 * color that should be used (if available) when starting or joining a Saros
 * session and for allowing the user to choose the visible annotations.
 * 
 * @author Maria Spiering
 * @author Stefan Rossbach
 * @author Vera Schlemm
 */
@Component(module = "prefs")
public final class AppearancePreferencePage extends PreferencePage implements
    IWorkbenchPreferencePage {

    // TODO move to a central class
    private static int DEFAULT_COLOR_ID = -1;

    @Inject
    private IPreferenceStore preferenceStore;

    private ColorChooser colorChooser;

    private int chosenFavoriteColorId = DEFAULT_COLOR_ID;

    private Button showContributionAnnotationButton;

    public AppearancePreferencePage() {
        SarosPluginContext.initComponent(this);
        setDescription(Messages.AppearancePreferencePage_appearance_settings);
    }

    @Override
    public void init(IWorkbench workbench) {
        // NOP
    }

    @Override
    public final boolean performOk() {
        if (chosenFavoriteColorId != DEFAULT_COLOR_ID) {
            preferenceStore.setValue(
                PreferenceConstants.FAVORITE_SESSION_COLOR_ID,
                chosenFavoriteColorId);
        }

        boolean showContribAnno = showContributionAnnotationButton
            .getSelection();

        preferenceStore.setValue(
            PreferenceConstants.SHOW_CONTRIBUTION_ANNOTATIONS, showContribAnno);

        return super.performOk();
    }

    @Override
    protected final void performDefaults() {
        preferenceStore
            .setToDefault(PreferenceConstants.FAVORITE_SESSION_COLOR_ID);

        colorChooser.selectColor(DEFAULT_COLOR_ID);

        preferenceStore
            .setToDefault(PreferenceConstants.SHOW_CONTRIBUTION_ANNOTATIONS);
        showContributionAnnotationButton.setSelection(preferenceStore
            .getBoolean(PreferenceConstants.SHOW_CONTRIBUTION_ANNOTATIONS));

        super.performDefaults();
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, true);
        composite.setLayout(layout);

        createColorGroup(composite);
        createAnnotationsVisibleControl(composite);
        return composite;
    }

    private void createAnnotationsVisibleControl(Composite parent) {
        final Group annotationsGroup = new Group(parent, SWT.NONE);
        annotationsGroup
            .setText(Messages.AppearancePreferencePage_annotations_group_label);
        annotationsGroup.setLayout(new GridLayout(1, false));
        annotationsGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
            false));

        showContributionAnnotationButton = new Button(annotationsGroup,
            SWT.CHECK);
        showContributionAnnotationButton
            .setText(Messages.AppearancePreferencePage_enable_contribution_annotation);
        showContributionAnnotationButton
            .setToolTipText(Messages.AppearancePreferencePage_show_contribution_annotations_tooltip);

        boolean isContribAnnoVisible = preferenceStore
            .getBoolean(PreferenceConstants.SHOW_CONTRIBUTION_ANNOTATIONS);
        showContributionAnnotationButton.setSelection(isContribAnnoVisible);
    }

    private void createColorGroup(Composite parent) {
        final Group colorGroup = new Group(parent, SWT.NONE);

        colorGroup.setText(Messages.AppearancePreferencePage_color);
        colorGroup.setLayout(new GridLayout(1, false));
        colorGroup
            .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        colorChooser = new ColorChooser(colorGroup, SWT.NONE);

        int currentColor = preferenceStore
            .getInt(PreferenceConstants.FAVORITE_SESSION_COLOR_ID);

        colorChooser.selectColor(currentColor);

        ColorSelectionListener colorSelectionListener = new ColorSelectionListener() {
            @Override
            public void selectionChanged(int colorId) {
                chosenFavoriteColorId = colorId;
            }
        };

        colorChooser.addSelectionListener(colorSelectionListener);
    }
}
