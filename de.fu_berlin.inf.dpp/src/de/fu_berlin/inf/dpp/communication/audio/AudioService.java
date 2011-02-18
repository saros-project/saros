package de.fu_berlin.inf.dpp.communication.audio;

import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.net.internal.StreamService;
import de.fu_berlin.inf.dpp.net.internal.StreamSession;
import de.fu_berlin.inf.dpp.ui.dialogs.ErrorMessageDialog;
import de.fu_berlin.inf.dpp.ui.util.DialogUtils;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * Implemented {#link StreamService} for VoIP function
 * 
 * @author ologa
 */
public class AudioService extends StreamService {

    private static final Logger log = Logger.getLogger(AudioService.class);

    protected AudioServiceManager audioServiceManager;

    // this chunkSize is just tested with Speex codec. The size of Speex frames
    // is 160, so that we should choose a value mod 160 = 0. During testing it
    // turned out that a chunkSize for 4 Speex frames is fine.
    @Override
    public int[] getChunkSize() {
        return new int[] { 640 };
    }

    @Override
    public String getServiceName() {
        return "AudioService";
    }

    // a VoIP session with 1000ms delay is nearly pointless, but during testing
    // i got some timeout errors with smaller values
    @Override
    public long[] getMaximumDelay() {
        return new long[] { 1000 };
    }

    @Override
    public int[] getBufferSize() {
        return new int[] { getChunkSize()[0] * 2 };
    }

    @Override
    public void startSession(StreamSession newSession) {
        audioServiceManager.startSession(newSession);
    }

    @Override
    public boolean sessionRequest(final User from, Object initial) {
        log.info(from + "wants to start a VoIP Session");

        // Is user already in a voip session and are the devices properly
        // configured?
        if (audioServiceManager.getStatus() == AudioServiceManager.VoIPStatus.RUNNING
            || audioServiceManager.getStatus() == AudioServiceManager.VoIPStatus.STOPPING) {
            return false;
        }

        if (!audioServiceManager.isPlaybackConfigured()) {
            ErrorMessageDialog
                .showErrorMessage("Your playback device is not properly configured. Please check the VoIP Settings at Window > Preferences > Saros > Communication. The VoIP session will NOT be started!");
            audioServiceManager.setPlaybackDeviceOk(false);
            return false;
        }

        Callable<Boolean> askUser;
        if (!audioServiceManager.isRecordConfigured()) {
            askUser = new Callable<Boolean>() {

                public Boolean call() throws Exception {

                    return DialogUtils
                        .openQuestionMessageDialog(
                            EditorAPI.getShell(),
                            "Incoming VoIP Invitation",
                            "Accept new VoIP Invitation from "
                                + from.getJID()
                                + " ?\n Warning: Your record device is not properly configured. Please check the VoIP Settings at Window > Preferences > Saros > Communication. The VoIP session can be started anyway. Maybe this session will be pointless if the other buddy has also no record device.");
                }
            };
            audioServiceManager.setRecordDeviceOk(false);
        } else {
            askUser = new Callable<Boolean>() {

                public Boolean call() throws Exception {

                    return DialogUtils.openQuestionMessageDialog(
                        EditorAPI.getShell(), "Incoming VoIP Invitation",
                        "Accept new VoIP Invitation from " + from.getJID()
                            + " ?");
                }
            };

        }
        try {
            return Utils.runSWTSync(askUser);
        } catch (Exception e) {
            log.error("Unexpected exception: ", e);
            return false;
        }

    }

    public void setAudioServiceManager(AudioServiceManager audioServiceManager) {
        this.audioServiceManager = audioServiceManager;
    }

}
