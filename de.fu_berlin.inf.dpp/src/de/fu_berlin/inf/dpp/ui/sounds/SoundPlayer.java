package de.fu_berlin.inf.dpp.ui.sounds;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.IPreferenceStore;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.util.ThreadUtils;

public class SoundPlayer {

    private static final Logger log = Logger.getLogger(SoundPlayer.class
        .getName());

    private final static int EXTERNAL_BUFFER_SIZE = 524288; // 128Kb

    @Inject
    private static IPreferenceStore store;

    static {
        SarosPluginContext.initComponent(new SoundPlayer());
    }

    public static void playSound(final String filename) {

        if (store == null
            || !store.getBoolean(PreferenceConstants.SOUND_ENABLED)) {
            return;
        }

        ThreadUtils.runSafeAsync("SoundPlayer", log, new Runnable() {
            @Override
            public void run() {
                playSoundInternal(filename);
            }
        });
    }

    private static void playSoundInternal(final String filename) {
        File soundFile = SoundManager.getSoundFile(filename);
        if ((soundFile == null) || !soundFile.exists()) {
            log.warn("Wave file not found at " + filename);
            return;
        }

        AudioInputStream audioInputStream = null;
        try {
            audioInputStream = AudioSystem.getAudioInputStream(soundFile);
        } catch (UnsupportedAudioFileException e1) {
            log.warn("Unsupported File: " + filename, e1);
            return;
        } catch (IOException e1) {
            log.error(
                "IO-Error while getting AudioInputStream for " + filename, e1);
            return;
        }

        AudioFormat format = audioInputStream.getFormat();
        SourceDataLine auline = null;
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

        try {
            auline = (SourceDataLine) AudioSystem.getLine(info);
            auline.open(format);
        } catch (LineUnavailableException e) {
            log.error("No Audioline available for " + filename, e);
            return;
        } catch (Exception e) {
            log.error("unknowen error while playing sound:" + filename, e);
            return;
        }

        auline.start();
        int nBytesRead = 0;
        byte[] abData = new byte[EXTERNAL_BUFFER_SIZE];

        try {
            while (nBytesRead != -1) {
                nBytesRead = audioInputStream.read(abData, 0, abData.length);
                if (nBytesRead >= 0)
                    auline.write(abData, 0, nBytesRead);
            }
        } catch (IOException e) {
            log.error("IO-Error while write auline for " + filename, e);
            return;
        } finally {
            auline.drain();
            auline.close();
        }
    }
}
