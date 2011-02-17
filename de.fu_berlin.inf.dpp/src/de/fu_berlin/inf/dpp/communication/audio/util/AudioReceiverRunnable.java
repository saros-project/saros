package de.fu_berlin.inf.dpp.communication.audio.util;

import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.xiph.speex.spi.Speex2PcmAudioInputStream;

import de.fu_berlin.inf.dpp.communication.audio.AudioServiceManager;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.ui.dialogs.ErrorMessageDialog;

/**
 * The {@link AudioReceiverRunnable} manages all packets which will be received
 * from the other user
 * 
 * @author ologa
 */
public class AudioReceiverRunnable {

    private static final Logger log = Logger
        .getLogger(AudioReceiverRunnable.class);

    protected AudioPlayer player;

    protected InputStream inputStream;
    protected AudioServiceManager audioServiceManager;
    protected PreferenceUtils preferenceUtils;
    protected Speex2PcmAudioInputStream decoderStream;
    protected Mixer mixer;
    protected boolean started = false;

    public AudioReceiverRunnable(InputStream in,
        AudioServiceManager audioServiceManager, PreferenceUtils preferenceUtils) {
        this.inputStream = in;
        this.audioServiceManager = audioServiceManager;
        this.preferenceUtils = preferenceUtils;

        mixer = preferenceUtils.getPlaybackMixer();

        player = new AudioPlayer(audioServiceManager);
        AudioFormat audioFormat = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED, 44100.0F, 16, 1, 2, 44100.0F,
            false);
        // Initialize the decoder.
        decoderStream = new Speex2PcmAudioInputStream(inputStream, audioFormat,
            AudioSystem.NOT_SPECIFIED);
        log.info("AudioDecoder successfully initalized.");
        if (player.setupSound(decoderStream, mixer) == false) {
            // log.error @ player.setupSound
            ErrorMessageDialog
                .showErrorMessage("Error while initializing the audio player. The VoIP Session will be stopped. Please check the VoIP Settings at Window > Preferences > Saros > Communication.");
            audioServiceManager.stopSession();
            return;
        }
        log.debug("AudioReceiver is ready!");
    }

    /**
     * <p>
     * <ol>
     * <li>Loading preference from preference store</li>
     * <li>Initalize the decoder & playback device (mixer)</li>
     * </ol>
     * </p>
     */

    public void start() {
        if (!started) {
            started = true;
            player.start();
        }
    }

    /**
     * Close all receiver / playback related Threads
     */
    public void stop() {
        if (started) {
            player.stop();
            log.debug("Player stopped!");
            IOUtils.closeQuietly(decoderStream);
            log.debug("AudioDecoder stopped!");
            mixer.close();
            started = false;
        }

    }

}
