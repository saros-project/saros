package saros.ui.preferencePages;

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
import saros.SarosPluginContext;
import saros.annotations.Component;
import saros.preferences.EclipsePreferenceConstants;
import saros.preferences.PreferenceConstants;
import saros.ui.Messages;
import saros.ui.widgets.ColorChooser;
import saros.ui.widgets.ColorChooser.ColorSelectionListener;

/**
 * This class is responsible for allowing the user to select his / her favorite color that should be
 * used (if available) when starting or joining a Saros session and for allowing the user to choose
 * the visible annotations.
 *
 * @author Maria Spiering
 * @author Stefan Rossbach
 * @author Vera Schlemm
 */
@Component(module = "prefs")
public final class PersonalizationPreferencePage extends PreferencePage
    implements IWorkbenchPreferencePage {

  // TODO move to a central class
  private static int DEFAULT_COLOR_ID = -1;

  @Inject private IPreferenceStore preferenceStore;

  private ColorChooser colorChooser;

  private int chosenFavoriteColorID = DEFAULT_COLOR_ID;

  private Button showBalloonNotifications;
  private Button showContributionAnnotation;
  private Button showSelectionFillUpAnnotation;

  private Button enableSoundEvents;
  private Button playSoundEventChatMessageSent;
  private Button playSoundEventChatMessageReceived;
  private Button playSoundEventContactComesOnline;
  private Button playSoundEventContactGoesOffline;

  public PersonalizationPreferencePage() {
    SarosPluginContext.initComponent(this);
  }

  @Override
  public void init(IWorkbench workbench) {
    // NOP
  }

  @Override
  public final boolean performOk() {
    if (chosenFavoriteColorID != DEFAULT_COLOR_ID) {
      preferenceStore.setValue(
          PreferenceConstants.FAVORITE_SESSION_COLOR_ID, chosenFavoriteColorID);
    }

    preferenceStore.setValue(
        EclipsePreferenceConstants.ENABLE_BALLOON_NOTIFICATION,
        showBalloonNotifications.getSelection());

    preferenceStore.setValue(
        EclipsePreferenceConstants.SHOW_CONTRIBUTION_ANNOTATIONS,
        showContributionAnnotation.getSelection());

    preferenceStore.setValue(
        EclipsePreferenceConstants.SHOW_SELECTIONFILLUP_ANNOTATIONS,
        showSelectionFillUpAnnotation.getSelection());

    preferenceStore.setValue(
        EclipsePreferenceConstants.SOUND_ENABLED, enableSoundEvents.getSelection());

    preferenceStore.setValue(
        EclipsePreferenceConstants.SOUND_PLAY_EVENT_MESSAGE_SENT,
        playSoundEventChatMessageSent.getSelection());

    preferenceStore.setValue(
        EclipsePreferenceConstants.SOUND_PLAY_EVENT_MESSAGE_RECEIVED,
        playSoundEventChatMessageReceived.getSelection());

    preferenceStore.setValue(
        EclipsePreferenceConstants.SOUND_PLAY_EVENT_CONTACT_ONLINE,
        playSoundEventContactComesOnline.getSelection());

    preferenceStore.setValue(
        EclipsePreferenceConstants.SOUND_PLAY_EVENT_CONTACT_OFFLINE,
        playSoundEventContactGoesOffline.getSelection());

    return super.performOk();
  }

  @Override
  protected final void performDefaults() {

    colorChooser.selectColor(DEFAULT_COLOR_ID);

    showBalloonNotifications.setSelection(
        preferenceStore.getDefaultBoolean(EclipsePreferenceConstants.ENABLE_BALLOON_NOTIFICATION));

    showContributionAnnotation.setSelection(
        preferenceStore.getDefaultBoolean(
            EclipsePreferenceConstants.SHOW_CONTRIBUTION_ANNOTATIONS));

    showSelectionFillUpAnnotation.setSelection(
        preferenceStore.getDefaultBoolean(
            EclipsePreferenceConstants.SHOW_SELECTIONFILLUP_ANNOTATIONS));

    enableSoundEvents.setSelection(
        preferenceStore.getDefaultBoolean(EclipsePreferenceConstants.SOUND_ENABLED));

    playSoundEventChatMessageSent.setSelection(
        preferenceStore.getDefaultBoolean(
            EclipsePreferenceConstants.SOUND_PLAY_EVENT_MESSAGE_SENT));

    playSoundEventChatMessageReceived.setSelection(
        preferenceStore.getDefaultBoolean(
            EclipsePreferenceConstants.SOUND_PLAY_EVENT_MESSAGE_RECEIVED));

    playSoundEventContactComesOnline.setSelection(
        preferenceStore.getDefaultBoolean(
            EclipsePreferenceConstants.SOUND_PLAY_EVENT_CONTACT_ONLINE));

    playSoundEventContactGoesOffline.setSelection(
        preferenceStore.getDefaultBoolean(
            EclipsePreferenceConstants.SOUND_PLAY_EVENT_CONTACT_OFFLINE));

    updateSoundEventButtonStates();

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
    group.setText(Messages.PersonalizationPreferencePage_visible_information_group_text);
    group.setLayout(new GridLayout(1, false));
    group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    showBalloonNotifications = new Button(group, SWT.CHECK);
    showBalloonNotifications.setText(
        Messages.PersonalizationPreferencePage_enable_balloon_notifications);

    showBalloonNotifications.setSelection(
        preferenceStore.getBoolean(EclipsePreferenceConstants.ENABLE_BALLOON_NOTIFICATION));

    showContributionAnnotation = new Button(group, SWT.CHECK);
    showContributionAnnotation.setText(
        Messages.PersonalizationPreferencePage_enable_contribution_annotation);

    showContributionAnnotation.setToolTipText(
        Messages.PersonalizationPreferencePage_show_contribution_annotations_tooltip);

    showContributionAnnotation.setSelection(
        preferenceStore.getBoolean(EclipsePreferenceConstants.SHOW_CONTRIBUTION_ANNOTATIONS));

    showSelectionFillUpAnnotation = new Button(group, SWT.CHECK);
    showSelectionFillUpAnnotation.setText(
        Messages.PersonalizationPreferencePage_enable_selectionfillup_annotation);

    showSelectionFillUpAnnotation.setToolTipText(
        Messages.PersonalizationPreferencePage_show_selectionfillup_annotations_tooltip);

    showSelectionFillUpAnnotation.setSelection(
        preferenceStore.getBoolean(EclipsePreferenceConstants.SHOW_SELECTIONFILLUP_ANNOTATIONS));
  }

  private void createSoundNotificationGroup(Composite parent) {
    final Group group = new Group(parent, SWT.NONE);

    group.setText(Messages.PersonalizationPreferencePage_sound_notification_group_text);
    group.setLayout(new GridLayout(1, false));
    group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    enableSoundEvents = new Button(group, SWT.CHECK);
    enableSoundEvents.setText(Messages.PersonalizationPreferencePage_enable_sound_button_text);

    enableSoundEvents.addSelectionListener(
        new SelectionAdapter() {
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
    chatEvents.setText(Messages.PersonalizationPreferencePage_sound_notification_chat_label_text);

    playSoundEventChatMessageReceived = new Button(soundEvents, SWT.CHECK);

    // col 2
    playSoundEventChatMessageReceived.setText(
        Messages.PersonalizationPreferencePage_enable_sound_on_message_receive_button_text);

    // col 3
    Label contactEvents = new Label(soundEvents, SWT.NONE);
    contactEvents.setText(
        Messages.PersonalizationPreferencePage_sound_notification_contacts_label_text);

    // col 4
    playSoundEventContactComesOnline = new Button(soundEvents, SWT.CHECK);

    playSoundEventContactComesOnline.setText(
        Messages.PersonalizationPreferencePage_enable_sound_on_contact_goes_online_button_text);

    // row 2

    // col 1 dummy
    new Label(soundEvents, SWT.NONE);

    // col 2
    playSoundEventChatMessageSent = new Button(soundEvents, SWT.CHECK);

    playSoundEventChatMessageSent.setText(
        Messages.PersonalizationPreferencePage_enable_sound_on_message_send_button_text);

    // col 3 dummy
    new Label(soundEvents, SWT.NONE);

    // col 4

    playSoundEventContactGoesOffline = new Button(soundEvents, SWT.CHECK);

    playSoundEventContactGoesOffline.setText(
        Messages.PersonalizationPreferencePage_enable_sound_on_contact_goes_offline_button_text);

    enableSoundEvents.setSelection(
        preferenceStore.getBoolean(EclipsePreferenceConstants.SOUND_ENABLED));

    playSoundEventChatMessageSent.setSelection(
        preferenceStore.getBoolean(EclipsePreferenceConstants.SOUND_PLAY_EVENT_MESSAGE_SENT));

    playSoundEventChatMessageReceived.setSelection(
        preferenceStore.getBoolean(EclipsePreferenceConstants.SOUND_PLAY_EVENT_MESSAGE_RECEIVED));

    playSoundEventContactComesOnline.setSelection(
        preferenceStore.getBoolean(EclipsePreferenceConstants.SOUND_PLAY_EVENT_CONTACT_ONLINE));

    playSoundEventContactGoesOffline.setSelection(
        preferenceStore.getBoolean(EclipsePreferenceConstants.SOUND_PLAY_EVENT_CONTACT_OFFLINE));

    updateSoundEventButtonStates();
  }

  private void createVisualAppearanceGroup(Composite parent) {
    final Group group = new Group(parent, SWT.NONE);

    group.setText(Messages.PersonalizationPreferencePage_visible_appearance_group_text);
    group.setLayout(new GridLayout(1, false));
    group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    new Label(group, SWT.NONE)
        .setText(Messages.PersonalizationPreferencePage_visible_appearance_set_color_label_text);

    colorChooser = new ColorChooser(group, SWT.NONE);

    int currentColor = preferenceStore.getInt(PreferenceConstants.FAVORITE_SESSION_COLOR_ID);

    colorChooser.selectColor(currentColor);

    ColorSelectionListener colorSelectionListener =
        new ColorSelectionListener() {
          @Override
          public void selectionChanged(int colorId) {
            chosenFavoriteColorID = colorId;
          }
        };

    colorChooser.addSelectionListener(colorSelectionListener);
  }

  private void updateSoundEventButtonStates() {
    boolean enabled = enableSoundEvents.getSelection();

    playSoundEventChatMessageSent.setEnabled(enabled);
    playSoundEventChatMessageReceived.setEnabled(enabled);
    playSoundEventContactComesOnline.setEnabled(enabled);
    playSoundEventContactGoesOffline.setEnabled(enabled);
  }
}
