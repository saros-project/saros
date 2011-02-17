package de.fu_berlin.inf.dpp.communication.audio.util;

import java.io.IOException;
import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.xiph.speex.spi.Pcm2SpeexAudioInputStream;

import de.fu_berlin.inf.dpp.communication.audio.AudioServiceManager;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.ui.dialogs.WarningMessageDialog;

/**
 * The {@link AudioSenderRunnable} manages all packets which will be send to the
 * other user
 * 
 * @author ologa
 */
public class AudioSenderRunnable implements Runnable {

    private static final Logger log = Logger
        .getLogger(AudioSenderRunnable.class);

    protected OutputStream outputStream;
    protected AudioRecorder recorder;
    protected Pcm2SpeexAudioInputStream encoderStream;

    protected AudioServiceManager audioServiceManager;
    protected PreferenceUtils preferenceUtils;
    protected Mixer mixer;
    protected boolean started = false;

    public AudioSenderRunnable(OutputStream out,
        AudioServiceManager audioServiceManager, PreferenceUtils preferenceUtils) {
        this.outputStream = out;
        this.audioServiceManager = audioServiceManager;
        this.preferenceUtils = preferenceUtils;
        recorder = new AudioRecorder();

        mixer = preferenceUtils.getRecordingMixer();
        if (recorder.setupSound(mixer) == false) {
            // log.error @ recorder.setupSound
            WarningMessageDialog
                .showWarningMessage(
                    "VoIP Warning",
                    "An error occured while initializing the audio record device. The VoIP Session will start anyway. Please check the VoIP Settings at Window > Preferences > Saros > Communication. Maybe this session will be pointless if the other buddy has also no record device.");
            log.warn("No record device initialized");
            return;
        }

        AudioFormat audioFormat = preferenceUtils.getEncodingFormat();
        log.debug("Starting Encoder with " + audioFormat.toString());
        log.info("AudioEncoder will be initalized.");
        encoderStream = new Pcm2SpeexAudioInputStream(
            recorder.getAudioInputStream(), audioFormat,
            AudioSystem.NOT_SPECIFIED);

        if (preferenceUtils.isDtxEnabled()) {
            encoderStream.getEncoder().setDtx(true);
        }
        log.info("AudioEncoder successfully initalized.");

        log.debug("AudioSender is ready");
    }

    /**
     * <p>
     * <ol>
     * <li>Loading preference from preference store</li>
     * <li>Initalize the encoder & record device (mixer)</li>
     * <li>read bytes from the decoder stream and put it on the OutputStream</li>
     * </ol>
     * </p>
     */
    public void run() {
        if (!started) {
            byte[] buffer = new byte[audioServiceManager.audioService
                .getChunkSize()[0]];
            while (audioServiceManager.getStatus() == AudioServiceManager.VoIPStatus.RUNNING
                && !Thread.interrupted()) {
                started = true;
                int offset = 0;
                int read = 0;
                while (offset < buffer.length) {
                    try {
                        read = encoderStream.read(buffer, offset, buffer.length
                            - offset);
                    } catch (IOException e) {
                        log.error("AudioEncoder is closed.", e);
                        return;
                    }
                    if (read != -1)
                        offset = offset + read;
                }
                try {
                    outputStream.write(buffer);
                } catch (IOException e) {
                    log.warn("OutputStream is closed!", e);
                    return;
                }
            }

        }
    }

    /**
     * Close all sender / record related Threads
     */
    public void stop() {
        if (started) {
            recorder.stop();
            log.debug("Recorder stopped!");
            IOUtils.closeQuietly(encoderStream);
            log.debug("Encoder stopped!");
            mixer.close();
            started = false;
        }
    }
}
