package de.fu_berlin.inf.dpp.communication.audio.util;

import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.communication.audio.AudioServiceManager;

/**
 * Write decoded bytes to {@link SourceDataLine} and play it.
 * 
 * @author ologa
 */
public class AudioPlayer {

    private static final Logger log = Logger.getLogger(AudioPlayer.class);

    protected AudioInputStream audioInputStream;
    protected AudioServiceManager audioServiceManager;
    protected SourceDataLine line;

    // Buffer to read from audioInputStream and write to SourceDataLine
    protected byte[] buffer;
    // Thread the player will run
    protected Thread playerThread;
    // player started?
    protected boolean started;

    public AudioPlayer(AudioServiceManager audioServiceManager) {
        this.audioServiceManager = audioServiceManager;
        started = false;
    }

    /**
     * Read bytes from the decoder stream and write it to the SourceDataLine
     */
    public void start() {
        line.start();
        log.debug("AudioReceiver started.");
        int continue_play;
        try {
            while (!Thread.interrupted()
                && audioServiceManager.getStatus() == AudioServiceManager.VoIPStatus.RUNNING
                && (continue_play = audioInputStream.read(buffer, 0,
                    buffer.length)) != -1) {

                if (continue_play > 0) {
                    int writtenbytes = line.write(buffer, 0, continue_play);
                    if (log.isTraceEnabled())
                        log.trace("written bytes to sourcedataline: "
                            + writtenbytes);

                }

            }
            line.stop();
            line.close();
        } catch (IOException e) {
            log.warn(
                "Incomplete Speex packet. Probably the Session is closed.", e);
        }
        log.debug("SourceDataLine will be closed...");
        IOUtils.closeQuietly(audioInputStream);
    }

    /**
     * 
     * @param in
     *            InputStream that can be casted to an {@link AudioInputStream}
     * @param mixer
     *            Playback device
     * 
     * @return true if player is successfully initialized, false if an error
     *         occured
     */
    public boolean setupSound(InputStream in, Mixer mixer) {
        if (started == false) {
            InputStream audioStream = in;
            AudioFormat audioFormat;
            if (mixer == null) {
                log.error("Playback device is already in use.");
                return false;
            }

            try {
                if (audioStream instanceof AudioInputStream) {
                    audioInputStream = (AudioInputStream) audioStream;
                } else {
                    audioInputStream = AudioSystem
                        .getAudioInputStream(audioStream);
                }
            } catch (Exception e) {
                log.error("InputStream is not an AudioStream", e);
                return false;
            }
            audioFormat = audioInputStream.getFormat();
            DataLine.Info info = new DataLine.Info(SourceDataLine.class,
                audioFormat);
            // If the audioFormat is not directly supported

            if (!AudioSystem.isLineSupported(info)) {
                AudioFormat targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED, 44100.0F, 16, 1, 2,
                    44100.0F, false);
                audioInputStream = AudioSystem.getAudioInputStream(
                    targetFormat, audioInputStream);
                audioFormat = audioInputStream.getFormat();
                info = new DataLine.Info(SourceDataLine.class, audioFormat);
            }

            try {
                line = (SourceDataLine) mixer.getLine(info);
                line.open(audioFormat);
            } catch (LineUnavailableException e) {
                log.error("SourceDataLine is already in use.", e);
                return false;
            }
            buffer = new byte[(int) audioFormat.getSampleRate()];
            started = true;
            return true;
        } else {
            log.error("Audio player already started!");
            return false;
        }

    }

    /**
     * Interrupt the AudioPlayer if session is closed
     */
    public void stop() {
        if (started)
            started = false;
        else
            log.error("Audio player is not running.");

    }
}