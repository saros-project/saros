package de.fu_berlin.inf.dpp.communication.audio;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.communication.audio.util.AudioReceiverRunnable;
import de.fu_berlin.inf.dpp.communication.audio.util.AudioSenderRunnable;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.exceptions.ConnectionException;
import de.fu_berlin.inf.dpp.exceptions.RemoteCancellationException;
import de.fu_berlin.inf.dpp.exceptions.StreamException;
import de.fu_berlin.inf.dpp.net.internal.StreamServiceManager;
import de.fu_berlin.inf.dpp.net.internal.StreamSession;
import de.fu_berlin.inf.dpp.net.internal.StreamSession.StreamSessionListener;
import de.fu_berlin.inf.dpp.observables.VoIPSessionObservable;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.ui.util.DialogUtils;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * <p>
 * This {@link AudioServiceManager} manages a VoIPSession which includes
 * inviting a user to a VoIP Session and starting or stopping a session.
 * </p>
 * <p>
 * Short introduction how to use this Manager:
 * <ol>
 * <li>Invite a new user to a VoIP session (
 * {@link AudioServiceManager#invite(User, SubMonitor)}</li>
 * <li>The invited user gets a popup dialog in
 * {@link AudioService#sessionRequest(User, Object)}</li>
 * <li>If he accepts the session
 * {@link AudioServiceManager#startSession(StreamSession)} will be called</li>
 * <li>Stop session with {@link AudioServiceManager#stopSession()} and the
 * {@link AudioServiceManager#audioStreamSessionListener} will be called</li>
 * </ol>
 * </p>
 * 
 * @author ologa
 */
public class AudioServiceManager {

    private static final Logger log = Logger
        .getLogger(AudioServiceManager.class);

    protected StreamSession session;
    public AudioService audioService;
    protected AudioSenderRunnable audioSenderRunnable;
    protected AudioReceiverRunnable audioReceiverRunnable;

    @Inject
    protected VoIPSessionObservable obs;

    protected StreamServiceManager streamServiceManager;
    protected PreferenceUtils preferenceUtils;

    protected boolean playbackDeviceOk = true;
    protected boolean recordDeviceOk = true;

    protected AudioServiceListenerDispatch audioListener = new AudioServiceListenerDispatch();

    public enum VoIPStatus {
        STOPPED, RUNNING, STOPPING;
    }

    protected VoIPStatus status = VoIPStatus.STOPPED;

    // Listener to shutdown the session
    StreamSessionListener audioStreamSessionListener = new StreamSessionListener() {

        /**
         * {@link StreamSessionListener#errorOccured(StreamException e)}
         * 
         * Display an error message an stop the running VoIP session
         */
        public void errorOccured(StreamException e) {
            stopSession();
            log.error(e.getMessage());
            Utils.runSafeSWTSync(log, new Runnable() {

                public void run() {
                    DialogUtils.openErrorMessageDialog(EditorAPI.getShell(),
                        "VoIP Session Error", "Unknown session error");
                }
            });
        }

        public void sessionStopped() {
            setStatus(VoIPStatus.STOPPING);
            log.debug("VoIP Status: STOPPING!");
            // inform audio listener about the event
            audioListener.sessionStopped(session);

            session.dispose();
            Utils.runSafeSWTAsync(log, new Runnable() {

                public void run() {
                    DialogUtils.openInformationMessageDialog(EditorAPI
                        .getShell(), "VoIP Session stopped",
                        "The VoIP Session has been stopped!");
                }
            });

            // shutdown Recorder, Player, Encoder + Decoder
            if (audioSenderRunnable != null)
                Utils.runSafeSync(log, new Runnable() {
                    public void run() {
                        audioSenderRunnable.stop();

                    }
                });

            if (audioReceiverRunnable != null)
                Utils.runSafeSync(log, new Runnable() {
                    public void run() {
                        audioReceiverRunnable.stop();
                    }
                });
            // Display an information message

            setStatus(VoIPStatus.STOPPED);
            log.debug("VoIP Status: STOPPED!");
            // Observer Value Listener to change the Icon
            obs.setValue(null);
            session.shutdownFinished();
        }

    };

    public AudioServiceManager(StreamServiceManager streamServiceManager,
        AudioService audioService, PreferenceUtils preferenceUtils) {
        this.streamServiceManager = streamServiceManager;
        this.audioService = audioService;
        this.audioService.setAudioServiceManager(this);
        this.preferenceUtils = preferenceUtils;

    }

    /**
     * Invite a target user to a new VoIP Session
     * 
     * @blocking until the buddy accepts or rejects the call
     */
    public IStatus invite(final User target, SubMonitor monitor) {

        switch (status) {
        case STOPPED:
            log.info("Inviting " + target.toString() + " to new VoIP Session");

            monitor.beginTask("VoIP Invite", 1);
            try {
                // TODO createSession should accept a SubMonitor
                session = streamServiceManager.createSession(audioService,
                    target, null, null);
                monitor.worked(1);
            } catch (InterruptedException e) {
                log.error(e.getMessage());
                Utils.runSafeSWTSync(log, new Runnable() {

                    public void run() {
                        DialogUtils.openErrorMessageDialog(EditorAPI
                            .getShell(), "VoIP Session Error",
                            "The Invitation was interrupted.");
                    }
                });
                return Status.CANCEL_STATUS;
            } catch (RemoteCancellationException e) {
                log.error(e.getMessage());
                Utils.runSafeSWTSync(log, new Runnable() {

                    public void run() {
                        DialogUtils.openErrorMessageDialog(EditorAPI
                            .getShell(), "VoIP Session was rejected", target
                            .getJID()
                            + " has not accepted your VoIP Invitation.");

                    }
                });

                return Status.CANCEL_STATUS;
            } catch (ConnectionException e) {
                log.error(e.getMessage());
                Utils.runSafeSWTSync(log, new Runnable() {

                    public void run() {
                        DialogUtils.openErrorMessageDialog(EditorAPI
                            .getShell(), "VoIP Session Error",
                            "Connection Error. Can't send any data!");
                    }
                });
                return Status.CANCEL_STATUS;
            } catch (TimeoutException e) {
                log.error(e.getMessage());
                Utils.runSafeSWTSync(log, new Runnable() {

                    public void run() {
                        DialogUtils.openErrorMessageDialog(EditorAPI
                            .getShell(), "VoIP Session Error",
                            "Timeout (1000ms) reached. Negotiation canceled");
                    }
                });
                return Status.CANCEL_STATUS;
            } catch (ExecutionException e) {
                log.error(e.getMessage());
                Utils.runSafeSWTSync(log, new Runnable() {

                    public void run() {
                        DialogUtils.openErrorMessageDialog(EditorAPI
                            .getShell(), "VoIP Session Error",
                            "Unkown Connection Error");
                    }
                });
                return Status.CANCEL_STATUS;
            } finally {
                monitor.done();
            }
            log.debug("Starting new VoIP Session...");
            Utils.runSafeAsync("VoIPSession", log, new Runnable() {
                public void run() {
                    startSession(session);
                }
            });
            return Status.OK_STATUS;
        case RUNNING:
            log.error("Cannot invite. Another VoIP Session is running!");
            return Status.CANCEL_STATUS;
        case STOPPING:
            log
                .error("Cannot invite. Another VoIP Session is stopping at the moment!");
            return Status.CANCEL_STATUS;
        }

        return Status.CANCEL_STATUS;

    }

    /**
     * Start {#link AudioSenderAction} and {#link AudioReceiverAction}
     * 
     * 
     * @param newSession
     *            Created session in
     *            {@link AudioServiceManager#invite(User, SubMonitor)} or
     *            {@link AudioService#startSession(StreamSession)}
     * 
     * 
     */
    public synchronized void startSession(StreamSession newSession) {

        if (getStatus() == VoIPStatus.STOPPED) {
            setStatus(VoIPStatus.RUNNING);
            log.debug("VoIP Status: RUNNING!");
            session = newSession;
            session.setListener(audioStreamSessionListener);
            obs.setValue(session);
            // inform audio listener about the event
            audioListener.startSession(newSession);

            if (recordDeviceOk) {
                audioSenderRunnable = new AudioSenderRunnable(session
                    .getOutputStream(0), this, preferenceUtils);
                Utils.runSafeAsync("audioSenderRunnable", log,
                    audioSenderRunnable);
            }
            if (playbackDeviceOk) {
                audioReceiverRunnable = new AudioReceiverRunnable(session
                    .getInputStream(0), this, preferenceUtils);
                audioReceiverRunnable.start();
            }

        } else {
            throw new IllegalStateException(
                "Another VoIP session is already started.");
        }

    }

    /**
     * End a VoIP Session
     */
    public void stopSession() {
        if (getStatus() != VoIPStatus.STOPPED) {
            log.debug("VoIP session will be stopped.");
            session.stopSession();
            // inform the audio listener about the event
            audioListener.stopSession(session);
        } else {
            log.error("stopSession called while no session is running");
        }
    }

    public VoIPStatus getStatus() {
        return status;
    }

    public void setStatus(VoIPStatus status) {
        this.status = status;
    }

    public boolean isPlaybackConfigured() {
        if (preferenceUtils.getPlaybackMixer() == null)
            return false;
        else
            return true;
    }

    public boolean isRecordConfigured() {
        if (preferenceUtils.getRecordingMixer() == null)
            return false;
        else
            return true;
    }

    public void setPlaybackDeviceOk(boolean playbackDeviceOk) {
        this.playbackDeviceOk = playbackDeviceOk;
    }

    public void setRecordDeviceOk(boolean recordDeviceOk) {
        this.recordDeviceOk = recordDeviceOk;
    }

    public void addAudioListener(IAudioServiceListener audioListener) {
        this.audioListener.add(audioListener);
    }

    public void remove(IAudioServiceListener audioListener) {
        this.audioListener.remove(audioListener);
    }

}
