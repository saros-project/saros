package de.fu_berlin.inf.dpp.ui.preferencePages;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
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

    private int chosenFavoriteColorID = DEFAULT_COLOR_ID;

    private Button showBalloonNotifications;
    private Button showContributionAnnotation;

    private Button enableSoundEvents;
    private Button playSoundEventChatMessageSent;
    private Button playSoundEventChatMessageReceived;
    private Button playSoundEventContactComesOnline;
    private Button playSoundEventContactGoesOffline;

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
        if (chosenFavoriteColorID != DEFAULT_COLOR_ID) {
            preferenceStore.setValue(
                PreferenceConstants.FAVORITE_SESSION_COLOR_ID,
                chosenFavoriteColorID);
        }

        preferenceStore.setValue(
            PreferenceConstants.ENABLE_BALLOON_NOTIFICATION,
            showBalloonNotifications.getSelection());

        preferenceStore.setValue(
            PreferenceConstants.SHOW_CONTRIBUTION_ANNOTATIONS,
            showContributionAnnotation.getSelection());

        preferenceStore.setValue(PreferenceConstants.SOUND_ENABLED,
            enableSoundEvents.getSelection());

        preferenceStore.setValue(
            PreferenceConstants.SOUND_PLAY_EVENT_MESSAGE_SENT,
            playSoundEventChatMessageSent.getSelection());

        preferenceStore.setValue(
            PreferenceConstants.SOUND_PLAY_EVENT_MESSAGE_RECEIVED,
            playSoundEventChatMessageReceived.getSelection());

        preferenceStore.setValue(
            PreferenceConstants.SOUND_PLAY_EVENT_CONTACT_ONLINE,
            playSoundEventContactComesOnline.getSelection());

        preferenceStore.setValue(
            PreferenceConstants.SOUND_PLAY_EVENT_CONTACT_OFFLINE,
            playSoundEventContactGoesOffline.getSelection());

        return super.performOk();
    }

    @Override
    protected final void performDefaults() {
        preferenceStore
            .setToDefault(PreferenceConstants.FAVORITE_SESSION_COLOR_ID);

        colorChooser.selectColor(DEFAULT_COLOR_ID);

        preferenceStore
            .setToDefault(PreferenceConstants.SHOW_CONTRIBUTION_ANNOTATIONS);
        showContributionAnnotation.setSelection(preferenceStore
            .getBoolean(PreferenceConstants.SHOW_CONTRIBUTION_ANNOTATIONS));

        preferenceStore
            .setToDefault(PreferenceConstants.ENABLE_BALLOON_NOTIFICATION);

        showBalloonNotifications.setSelection(preferenceStore
            .getBoolean(PreferenceConstants.ENABLE_BALLOON_NOTIFICATION));

        preferenceStore.setToDefault(PreferenceConstants.SOUND_ENABLED);

        preferenceStore
            .setToDefault(PreferenceConstants.SOUND_PLAY_EVENT_MESSAGE_SENT);

        preferenceStore
            .setToDefault(PreferenceConstants.SOUND_PLAY_EVENT_MESSAGE_RECEIVED);

        preferenceStore
            .setToDefault(PreferenceConstants.SOUND_PLAY_EVENT_CONTACT_ONLINE);

        preferenceStore
            .setToDefault(PreferenceConstants.SOUND_PLAY_EVENT_CONTACT_OFFLINE);

        initializeSoundButtons();

        super.performDefaults();
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, true);
        layout.verticalSpacing = 15;
        composite.setLayout(layout);

        createSoundNotificationGroup(composite);
        createVisibleInformationGroup(composite);
        createVisualAppearanceGroup(composite);
        return composite;
    }

    private void createVisibleInformationGroup(Composite parent) {
        final Group group = new Group(parent, SWT.NONE);
        group.setText("Visual Information");
        group.setLayout(new GridLayout(1, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        showBalloonNotifications = new Button(group, SWT.CHECK);
        showBalloonNotifications
            .setText(Messages.AppearancePreferencePage_enable_balloon_notifications);

        showBalloonNotifications.setSelection(preferenceStore
            .getBoolean(PreferenceConstants.ENABLE_BALLOON_NOTIFICATION));

        showContributionAnnotation = new Button(group, SWT.CHECK);
        showContributionAnnotation
            .setText(Messages.AppearancePreferencePage_enable_contribution_annotation);

        showContributionAnnotation
            .setToolTipText(Messages.AppearancePreferencePage_show_contribution_annotations_tooltip);

        showContributionAnnotation.setSelection(preferenceStore
            .getBoolean(PreferenceConstants.SHOW_CONTRIBUTION_ANNOTATIONS));
    }

    private void createSoundNotificationGroup(Composite parent) {
        final Group group = new Group(parent, SWT.NONE);

        group.setText("Sound Notifications");
        group.setLayout(new GridLayout(1, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        enableSoundEvents = new Button(group, SWT.CHECK);
        enableSoundEvents.setText("Enabled Sound Notifications");

        enableSoundEvents.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                updateSoundEventButtonStates();
            }
        });

        Composite soundEvents = new Composite(group, SWT.NONE);
        GridLayout layout = new GridLayout(4, false);
        layout.horizontalSpacing = 8;
        layout.marginWidth = 0;

        soundEvents.setLayout(layout);

        // row 1

        // col 1
        Label chatEvents = new Label(soundEvents, SWT.NONE);
        chatEvents.setText("Chat");

        playSoundEventChatMessageReceived = new Button(soundEvents, SWT.CHECK);

        // col 2
        playSoundEventChatMessageReceived.setText("receiving message");

        // col 3
        Label contactEvents = new Label(soundEvents, SWT.NONE);
        contactEvents.setText("Contacts");

        // col 4
        playSoundEventContactComesOnline = new Button(soundEvents, SWT.CHECK);

        playSoundEventContactComesOnline.setText("going online");

        // row 2

        // col 1 dummy
        new Label(soundEvents, SWT.NONE);

        // col 2
        playSoundEventChatMessageSent = new Button(soundEvents, SWT.CHECK);

        playSoundEventChatMessageSent.setText("sending message");

        // col 3 dummy
        new Label(soundEvents, SWT.NONE);

        // col 4

        playSoundEventContactGoesOffline = new Button(soundEvents, SWT.CHECK);

        playSoundEventContactGoesOffline.setText("going offline");

        initializeSoundButtons();

    }

    private void createVisualAppearanceGroup(Composite parent) {
        final Group group = new Group(parent, SWT.NONE);

        group.setText("Visual Appearance");
        group.setLayout(new GridLayout(1, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        new Label(group, SWT.NONE).setText("Set Preferred Color");

        colorChooser = new ColorChooser(group, SWT.NONE);

        int currentColor = preferenceStore
            .getInt(PreferenceConstants.FAVORITE_SESSION_COLOR_ID);

        colorChooser.selectColor(currentColor);

        ColorSelectionListener colorSelectionListener = new ColorSelectionListener() {
            @Override
            public void selectionChanged(int colorId) {
                chosenFavoriteColorID = colorId;
            }
        };

        colorChooser.addSelectionListener(colorSelectionListener);
    }

    private void initializeSoundButtons() {

        enableSoundEvents.setSelection(preferenceStore
            .getBoolean(PreferenceConstants.SOUND_ENABLED));

        playSoundEventChatMessageSent.setSelection(preferenceStore
            .getBoolean(PreferenceConstants.SOUND_PLAY_EVENT_MESSAGE_SENT));

        playSoundEventChatMessageReceived.setSelection(preferenceStore
            .getBoolean(PreferenceConstants.SOUND_PLAY_EVENT_MESSAGE_RECEIVED));

        playSoundEventContactComesOnline.setSelection(preferenceStore
            .getBoolean(PreferenceConstants.SOUND_PLAY_EVENT_CONTACT_ONLINE));

        playSoundEventContactGoesOffline.setSelection(preferenceStore
            .getBoolean(PreferenceConstants.SOUND_PLAY_EVENT_CONTACT_OFFLINE));

        updateSoundEventButtonStates();
    }

    private void updateSoundEventButtonStates() {
        boolean enabled = enableSoundEvents.getSelection();

        playSoundEventChatMessageSent.setEnabled(enabled);
        playSoundEventChatMessageReceived.setEnabled(enabled);
        playSoundEventContactComesOnline.setEnabled(enabled);
        playSoundEventContactGoesOffline.setEnabled(enabled);
    }
}
