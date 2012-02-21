package de.fu_berlin.inf.dpp.communication.audio;

import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;

/**
 * MixerManager maps Mixer.Info to a String to use this String on the
 * PreferencePage
 * 
 * @author ologa
 * @author Stefan Rossbach
 */
public class MixerManager {

    // TODO Mixer.Info should be stored in preferences rather than just the
    // Strings

    private static final int INPUT_MIXER = 1;
    private static final int OUTPUT_MIXER = 2;

    public MixerManager() {
        // nop
    }

    public List<Mixer.Info> getRecordingMixers() {
        return getMixerInfos(INPUT_MIXER);
    }

    public List<Mixer.Info> getPlaybackMixers() {
        return getMixerInfos(OUTPUT_MIXER);
    }

    public Mixer getMixerByName(String name) {
        for (Mixer.Info mixerInfo : AudioSystem.getMixerInfo())
            if (mixerInfo.getName().equals(name))
                return AudioSystem.getMixer(mixerInfo);

        return null;
    }

    private List<Mixer.Info> getMixerInfos(int type) {
        List<Mixer.Info> mixerInfos = new ArrayList<Mixer.Info>();

        for (Mixer.Info mixerInfo : AudioSystem.getMixerInfo()) {

            // On some machines are Software Mixer whose names begin with "PORT"
            // We do not want Software Mixer here
            if (mixerInfo.getName().toUpperCase().contains("PORT"))
                continue;

            Mixer mixer = AudioSystem.getMixer(mixerInfo);

            if (mixer.getSourceLineInfo().length > 0
                && (type & OUTPUT_MIXER) == OUTPUT_MIXER) {
                mixerInfos.add(mixerInfo);
            }

            if (mixer.getTargetLineInfo().length > 0
                && (type & INPUT_MIXER) == INPUT_MIXER) {
                mixerInfos.add(mixerInfo);
            }
        }

        return mixerInfos;
    }
}
