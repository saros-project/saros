package de.fu_berlin.inf.dpp.ui.preferencePages;

import javax.sound.sampled.Mixer;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.communication.audio.MixerManager;
import de.fu_berlin.inf.dpp.preferences.AudioSettings;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;

@Component(module = "prefs")
public class CommunicationPreferencePage extends FieldEditorPreferencePage
    implements IWorkbenchPreferencePage {

    @Inject
    protected Saros saros;

    @Inject
    protected MixerManager mixerManager;

    protected Composite parent;
    protected StringFieldEditor chatserver;
    protected BooleanFieldEditor beepUponIM;
    protected StringFieldEditor audioQuality;
    protected BooleanFieldEditor audio_vbr;
    protected BooleanFieldEditor audio_dtx;

    protected IPreferenceStore prefs;

    public CommunicationPreferencePage() {
        super(FieldEditorPreferencePage.GRID);
        SarosPluginContext.initComponent(this);
        setPreferenceStore(saros.getPreferenceStore());
        setDescription("Settings for Chat and VoIP Functionality.");
        this.prefs = saros.getPreferenceStore();
    }

    @Override
    protected void createFieldEditors() {

        parent = getFieldEditorParent();

        addField(new StringFieldEditor(PreferenceConstants.SKYPE_USERNAME,
            "Skype name:", getFieldEditorParent()));

        chatserver = new StringFieldEditor(PreferenceConstants.CHATSERVER,
            "Chatserver (Example: conference.jabber.org)", parent);

        beepUponIM = new BooleanFieldEditor(PreferenceConstants.BEEP_UPON_IM,
            "Beep when receiving a chat message", parent);

        beepUponIM.setEnabled(true, parent);

        audioQuality = new StringFieldEditor(
            PreferenceConstants.AUDIO_QUALITY_LEVEL,
            "Audio Quality Level (0-10) - 10 is best", parent);

        audioQuality.setEmptyStringAllowed(false);
        audioQuality.setTextLimit(2);

        ComboFieldEditor audioSamplerate = new ComboFieldEditor(
            PreferenceConstants.AUDIO_SAMPLERATE, "Audio Samplerate",
            get2dArray(AudioSettings.AUDIO_SAMPLE_RATE), parent);

        audio_vbr = new BooleanFieldEditor(PreferenceConstants.AUDIO_VBR,
            "Use Variable Bitrate", parent);

        audio_dtx = new BooleanFieldEditor(
            PreferenceConstants.AUDIO_ENABLE_DTX,
            "Use Discontinuous Transmission (only works with VBR)", parent);

        audio_dtx.setEnabled(prefs.getBoolean(PreferenceConstants.AUDIO_VBR),
            parent);

        ComboFieldEditor audioPlaybackDevs = new ComboFieldEditor(
            PreferenceConstants.AUDIO_PLAYBACK_DEVICE, "Audio Playback Device",
            getPlaybackMixersString(), parent);

        ComboFieldEditor audioRecordDevs = new ComboFieldEditor(
            PreferenceConstants.AUDIO_RECORD_DEVICE, "Audio Record Device",
            getRecordMixersString(), parent);

        addField(chatserver);
        addField(beepUponIM);
        addField(audioQuality);
        addField(audioSamplerate);
        addField(audio_vbr);
        addField(audio_dtx);
        addField(audioPlaybackDevs);
        addField(audioRecordDevs);

    }

    public void init(IWorkbench arg0) {
        // NOP
    }

    @Override
    protected void checkState() {
        int audioQualityCheck = -1;
        super.checkState();

        if (!isValid())
            return;

        try {
            audioQualityCheck = Integer.parseInt(audioQuality.getStringValue());
        } catch (NumberFormatException e) {
            setErrorMessage("Audio Quality has to be an Integer value!");
            setValid(false);
            return;
        }

        if (chatserver.getStringValue() == "") {
            setErrorMessage("Chatserver Field is empty!");
            setValid(false);
        } else if (audioQualityCheck < 0 || audioQualityCheck > 10) {
            setErrorMessage("Audio Quality has to be >= 0 and <= 10");
            setValid(false);
        } else {
            setErrorMessage(null);
            setValid(true);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {

        if (event.getProperty().equals(FieldEditor.VALUE)) {
            if (event.getSource().equals(chatserver)
                || event.getSource().equals(audioQuality))
                checkState();
        }

        if (event.getSource() instanceof FieldEditor) {
            FieldEditor field = (FieldEditor) event.getSource();

            if (field.getPreferenceName().equals(PreferenceConstants.AUDIO_VBR)) {
                if (event.getNewValue() instanceof Boolean) {
                    Boolean newValue = (Boolean) event.getNewValue();
                    audio_dtx.setEnabled(newValue, parent);
                }
            }
        }

    }

    protected String[][] getRecordMixersString() {
        Mixer.Info mixerInfo[] = mixerManager.getRecordingMixers();
        String recordDevices[][];
        if (mixerInfo.length == 0) {
            recordDevices = new String[1][2];
            recordDevices[0][0] = "";
            recordDevices[0][1] = "UNKNOWN";
        } else {
            recordDevices = new String[mixerInfo.length][2];
            for (int i = 0; i < mixerInfo.length; i++) {
                recordDevices[i][0] = mixerInfo[i].getName();
                recordDevices[i][1] = mixerInfo[i].getName();
            }
        }
        return recordDevices;
    }

    protected String[][] getPlaybackMixersString() {
        Mixer.Info mixerInfo[] = mixerManager.getPlaybackMixers();
        String playbackDevices[][];
        if (mixerInfo.length == 0) {
            playbackDevices = new String[1][2];
            playbackDevices[0][0] = "";
            playbackDevices[0][1] = "UNKNOWN";
        } else {
            playbackDevices = new String[mixerInfo.length][2];
            for (int i = 0; i < mixerInfo.length; i++) {
                playbackDevices[i][0] = mixerInfo[i].getName();
                playbackDevices[i][1] = mixerInfo[i].getName();
            }
        }
        return playbackDevices;
    }

    protected String[][] get2dArray(String[] inputArray) {
        String outputArray[][] = new String[inputArray.length][2];
        for (int i = 0; i < inputArray.length; i++) {
            outputArray[i][0] = inputArray[i];
            outputArray[i][1] = inputArray[i];
        }
        return outputArray;
    }

}
