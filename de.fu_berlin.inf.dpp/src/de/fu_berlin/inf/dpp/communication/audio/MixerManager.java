package de.fu_berlin.inf.dpp.communication.audio;

import java.util.HashMap;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;

/**
 * MixerManager maps Mixer.Info to a String to use this String on the
 * PreferencePage
 * 
 * @author ologa
 */
public class MixerManager {

    // TODO Mixer.Info should be stored in preferences rather than just the
    // Strings
    HashMap<String, Mixer.Info> recordDevices = new HashMap<String, Mixer.Info>();
    HashMap<String, Mixer.Info> playbackDevices = new HashMap<String, Mixer.Info>();

    public MixerManager() {
        initializeMixerInfo();
    }

    public Mixer.Info[] getRecordingMixers() {
        return recordDevices.values().toArray(new Mixer.Info[0]);
    }

    public Mixer.Info[] getPlaybackMixers() {
        return playbackDevices.values().toArray(new Mixer.Info[0]);
    }

    public Mixer getMixerByName(String mixerInfo) {
        Mixer mixer = null;
        if (recordDevices.containsKey(mixerInfo)) {
            mixer = AudioSystem.getMixer(recordDevices.get(mixerInfo));
        }
        if (playbackDevices.containsKey(mixerInfo)) {
            mixer = AudioSystem.getMixer(playbackDevices.get(mixerInfo));
        }
        if (mixer == null) {
            return null;
        } else {
            return mixer;
        }
    }

    protected void initializeMixerInfo() {
        Mixer.Info mixerInfo[] = AudioSystem.getMixerInfo();
        for (int i = 0; i < mixerInfo.length; i++) {
            Mixer tempmixer = AudioSystem.getMixer(mixerInfo[i]);
            // On some machines are Software Mixer whose names begin with "PORT"
            // We dont want Software Mixer here
            if (!mixerInfo[i].getName().contains("Port")) {
                if (tempmixer.getSourceLineInfo().length > 0) {
                    playbackDevices.put(mixerInfo[i].getName(), mixerInfo[i]);
                } else if (tempmixer.getTargetLineInfo().length > 0) {
                    recordDevices.put(mixerInfo[i].getName(), mixerInfo[i]);
                }
            }
        }
    }
}
