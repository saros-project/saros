package de.fu_berlin.inf.dpp.ui.preferencePages;

import java.util.Collections;
import java.util.List;

import javax.sound.sampled.Mixer;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.communication.audio.MixerManager;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.ui.Messages;

//FIXME the layout of this page is completely BROKEN !!!
@Component(module = "prefs")
public class CommunicationPreferencePage extends FieldEditorPreferencePage
    implements IWorkbenchPreferencePage {

    // TODO put this into the preference store
    private static final String[] AUDIO_SAMPLE_RATE = new String[] { "44100",
        "32000", "16000" };

    private static final String[][] AUDIO_QUALITY_VALUES = { { "0", "0" },
        { "1", "1" }, { "2", "2" }, { "3", "3" }, { "4", "4" }, { "5", "5" },
        { "6", "6" }, { "7", "7" }, { "8", "8" }, { "9", "9" }, { "10", "10" } };

    @Inject
    private IPreferenceStore prefs;

    @Inject
    private MixerManager mixerManager;

    private BooleanFieldEditor enableSoundEvents;

    private BooleanFieldEditor playSoundEventChatMessageSent;
    private BooleanFieldEditor playSoundEventChatMessageReceived;

    private BooleanFieldEditor playSoundEventContactComesOnline;
    private BooleanFieldEditor playSoundEventContactGoesOffline;

    private StringFieldEditor chatserver;
    private BooleanFieldEditor useCustomChatServer;
    private StringFieldEditor skypeName;

    private BooleanFieldEditor audio_vbr;
    private BooleanFieldEditor audio_dtx;
    private ComboFieldEditor audioQuality;

    private Group soundGroup;
    private Group chatGroup;
    private Group voipGroup;
    private Composite chatServerGroup;

    public CommunicationPreferencePage() {
        super(FieldEditorPreferencePage.GRID);
        SarosPluginContext.initComponent(this);
        setPreferenceStore(prefs);
        setDescription("Settings for Chat and VoIP Functionality.");
    }

    @Override
    public void init(IWorkbench workbench) {
        // NOP
    }

    @Override
    protected void createFieldEditors() {
        soundGroup = new Group(getFieldEditorParent(), SWT.NONE);
        chatGroup = new Group(getFieldEditorParent(), SWT.NONE);
        voipGroup = new Group(getFieldEditorParent(), SWT.NONE);

        GridData soundGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        GridData chatGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        GridData voipGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);

        soundGridData.horizontalSpan = 2;
        chatGridData.horizontalSpan = 2;
        voipGridData.horizontalSpan = 2;

        soundGroup.setText("Sounds");
        soundGroup.setLayout(new GridLayout(1, false));

        chatGroup.setText("Chat");
        chatGroup.setLayout(new GridLayout(2, false));

        voipGroup.setText("VoIP");
        voipGroup.setLayout(new GridLayout(2, false));

        soundGroup.setLayoutData(soundGridData);
        chatGroup.setLayoutData(chatGridData);
        voipGroup.setLayoutData(voipGridData);

        enableSoundEvents = new BooleanFieldEditor(
            PreferenceConstants.SOUND_ENABLED, "Enable sound events",
            soundGroup);

        playSoundEventChatMessageSent = new BooleanFieldEditor(
            PreferenceConstants.SOUND_PLAY_EVENT_MESSAGE_SENT,
            "Play notification when sending a message", soundGroup);

        playSoundEventChatMessageReceived = new BooleanFieldEditor(
            PreferenceConstants.SOUND_PLAY_EVENT_MESSAGE_RECEIVED,
            "Play notification when receiving a message", soundGroup);

        playSoundEventContactComesOnline = new BooleanFieldEditor(
            PreferenceConstants.SOUND_PLAY_EVENT_CONTACT_ONLINE,
            "Play notification when contact comes online", soundGroup);

        playSoundEventContactGoesOffline = new BooleanFieldEditor(
            PreferenceConstants.SOUND_PLAY_EVENT_CONTACT_OFFLINE,
            "Play notification when contact goes offline", soundGroup);

        chatServerGroup = new Composite(chatGroup, SWT.NONE);
        chatServerGroup.setLayout(new GridLayout(2, false));
        chatServerGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
            false));

        chatserver = new StringFieldEditor(
            PreferenceConstants.CUSTOM_MUC_SERVICE, "Custom chatserver: ",
            chatServerGroup);

        useCustomChatServer = new BooleanFieldEditor(
            PreferenceConstants.FORCE_CUSTOM_MUC_SERVICE,
            "Always use custom chatserver", chatGroup);

        skypeName = new StringFieldEditor(PreferenceConstants.SKYPE_USERNAME,
            "Skype name:", getFieldEditorParent());

        audioQuality = new ComboFieldEditor(
            PreferenceConstants.AUDIO_QUALITY_LEVEL,
            "Audio Quality Level (0-10) - 10 is best", AUDIO_QUALITY_VALUES,
            voipGroup);

        ComboFieldEditor audioSamplerate = new ComboFieldEditor(
            PreferenceConstants.AUDIO_SAMPLERATE, "Audio Samplerate (kHz)",
            get2dArray(AUDIO_SAMPLE_RATE), voipGroup);

        audio_vbr = new BooleanFieldEditor(
            PreferenceConstants.AUDIO_VBR,
            "Use Variable Bitrate (gives a better quality-to-space ratio, but may introduce a delay)",
            voipGroup);

        audio_dtx = new BooleanFieldEditor(
            PreferenceConstants.AUDIO_ENABLE_DTX,
            "Use Discontinuous Transmission (silence is not transmitted - only works with variable bitrate)",
            voipGroup);

        audio_dtx.setEnabled(prefs.getBoolean(PreferenceConstants.AUDIO_VBR),
            voipGroup);

        boolean enabled = true;

        String[][] playbackMixers = getPlaybackMixerNames();

        if (playbackMixers == null) {
            playbackMixers = new String[][] { { "N/A",
                Messages.CommunicationPreferencePage_unknown } };
            enabled = false;
        }

        ComboFieldEditor audioPlaybackDevices = new ComboFieldEditor(
            PreferenceConstants.AUDIO_PLAYBACK_DEVICE, "Audio Playback Device",
            playbackMixers, voipGroup);

        audioPlaybackDevices.setEnabled(enabled, voipGroup);

        enabled = true;

        String[][] recordMixers = getRecordMixerNames();

        if (recordMixers == null) {
            recordMixers = new String[][] { { "N/A",
                Messages.CommunicationPreferencePage_unknown } };
            enabled = false;
        }

        ComboFieldEditor audioRecordDevices = new ComboFieldEditor(
            PreferenceConstants.AUDIO_RECORD_DEVICE, "Audio Record Device",
            recordMixers, voipGroup);

        audioRecordDevices.setEnabled(enabled, voipGroup);

        addField(enableSoundEvents);
        addField(playSoundEventChatMessageSent);
        addField(playSoundEventChatMessageReceived);
        addField(playSoundEventContactComesOnline);
        addField(playSoundEventContactGoesOffline);

        addField(chatserver);
        addField(useCustomChatServer);
        addField(skypeName);
        addField(audioQuality);
        addField(audioSamplerate);
        addField(audio_vbr);
        addField(audio_dtx);
        addField(audioPlaybackDevices);
        addField(audioRecordDevices);

    }

    @Override
    public void initialize() {
        super.initialize();
        if (prefs.getBoolean(PreferenceConstants.FORCE_CUSTOM_MUC_SERVICE)) {
            useCustomChatServer.setEnabled(!chatserver.getStringValue()
                .isEmpty(), chatGroup);
        }

        updateSoundEventEditors();
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {

        if (event.getSource() instanceof FieldEditor) {
            FieldEditor field = (FieldEditor) event.getSource();

            if (field.getPreferenceName().equals(PreferenceConstants.AUDIO_VBR)) {
                if (event.getNewValue() instanceof Boolean) {
                    Boolean newValue = (Boolean) event.getNewValue();
                    audio_dtx.setEnabled(newValue, voipGroup);
                }
            } else if (field.getPreferenceName().equals(
                PreferenceConstants.CUSTOM_MUC_SERVICE)) {
                String serverName = event.getNewValue().toString();
                useCustomChatServer
                    .setEnabled(!serverName.isEmpty(), chatGroup);
            } else if (field == enableSoundEvents)
                updateSoundEventEditors();
        }
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();
        updateSoundEventEditors();
    }

    private String[][] getRecordMixerNames() {
        return getMixerNames(0);
    }

    private String[][] getPlaybackMixerNames() {
        return getMixerNames(1);
    }

    @SuppressWarnings("unchecked")
    private String[][] getMixerNames(int type) {
        List<Mixer.Info> mixerInfo;
        if (type == 0)
            mixerInfo = mixerManager.getRecordingMixers();
        else if (type == 1)
            mixerInfo = mixerManager.getPlaybackMixers();
        else
            mixerInfo = Collections.EMPTY_LIST;

        if (mixerInfo.isEmpty())
            return null;

        String[][] devices = new String[mixerInfo.size()][2];
        for (int i = 0; i < mixerInfo.size(); i++) {
            devices[i][0] = mixerInfo.get(i).getName();
            devices[i][1] = mixerInfo.get(i).getName();
        }

        return devices;

    }

    private String[][] get2dArray(String[] inputArray) {
        String outputArray[][] = new String[inputArray.length][2];
        for (int i = 0; i < inputArray.length; i++) {
            outputArray[i][0] = inputArray[i];
            outputArray[i][1] = inputArray[i];
        }
        return outputArray;
    }

    private void updateSoundEventEditors() {
        boolean enabled = enableSoundEvents.getBooleanValue();

        playSoundEventChatMessageSent.setEnabled(enabled, soundGroup);
        playSoundEventChatMessageReceived.setEnabled(enabled, soundGroup);
        playSoundEventContactComesOnline.setEnabled(enabled, soundGroup);
        playSoundEventContactGoesOffline.setEnabled(enabled, soundGroup);
    }
}
