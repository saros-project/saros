package de.fu_berlin.inf.dpp.communication.audio.util;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

import org.apache.log4j.Logger;

/**
 * Initialize the TargetDataLine to record audio and encode it.
 * 
 * @author ologa
 */
public class AudioRecorder {

    private static final Logger log = Logger.getLogger(AudioRecorder.class);

    protected AudioInputStream audioInputStream;
    protected TargetDataLine line;
    protected boolean started;

    public AudioRecorder() {
        started = false;
    }

    /**
     * Stop the playback thread and destroy all resources.
     */
    public void stop() {
        if (started) {
            log.debug("TargetDataLine will be closed!");
            line.stop();
            line.close();
            started = false;
        } else {
            log.error("Audio recorder already stopped");
        }
    }

    /**
     * Initialize the TargetDataLine
     * 
     * @param mixer
     *            Record device
     * @return true if the recorder is successfully initialized, false if an
     *         error occured
     */
    public boolean setupSound(Mixer mixer) {
        DataLine.Info info;
        if (started == false) {
            AudioFormat audioFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED, 44100.0F, 16, 1, 2, 44100.0F,
                false);

            info = new DataLine.Info(TargetDataLine.class, audioFormat);
            try {
                line = (TargetDataLine) mixer.getLine(info);
                line.open(audioFormat);
            } catch (LineUnavailableException e) {
                log.error("TargetDataLine is already in use.");
                return false;
            }
            audioInputStream = new AudioInputStream(line);
            audioInputStream = AudioSystem.getAudioInputStream(audioFormat,
                audioInputStream);
            audioFormat = audioInputStream.getFormat();
            line.start();
            started = true;
            return true;
        } else {
            log.error("Audio recorder already started!");
            return false;
        }
    }

    /**
     * 
     * @return current AudioInputStream for this AudioRecorder
     */
    public AudioInputStream getAudioInputStream() {
        return audioInputStream;

    }
}
