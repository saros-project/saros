/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universit�t Berlin - Fachbereich Mathematik und Informatik - 2010
 * (c) Stephan Lau - 2010
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.fu_berlin.inf.dpp.videosharing;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IViewPart;
import org.picocontainer.Disposable;
import org.picocontainer.annotations.Inject;

import com.thoughtworks.xstream.InitializationException;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.exceptions.ConnectionException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.exceptions.StreamException;
import de.fu_berlin.inf.dpp.net.internal.StreamServiceManager;
import de.fu_berlin.inf.dpp.net.internal.StreamSession;
import de.fu_berlin.inf.dpp.net.internal.StreamSession.StreamSessionListener;
import de.fu_berlin.inf.dpp.observables.SarosSessionObservable;
import de.fu_berlin.inf.dpp.observables.VideoSessionObservable;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.ui.util.DialogUtils;
import de.fu_berlin.inf.dpp.ui.views.VideoPlayerView;
import de.fu_berlin.inf.dpp.util.Utils;
import de.fu_berlin.inf.dpp.videosharing.activities.ImageSourceSwitchModeVideoActivity;
import de.fu_berlin.inf.dpp.videosharing.activities.SessionErrorVideoActivity;
import de.fu_berlin.inf.dpp.videosharing.activities.SessionVideoActivity;
import de.fu_berlin.inf.dpp.videosharing.decode.Decoder;
import de.fu_berlin.inf.dpp.videosharing.encode.Encoder;
import de.fu_berlin.inf.dpp.videosharing.exceptions.DecoderInitializationException;
import de.fu_berlin.inf.dpp.videosharing.exceptions.EncoderInitializationException;
import de.fu_berlin.inf.dpp.videosharing.manager.ActivityManager;
import de.fu_berlin.inf.dpp.videosharing.manager.ConnectionManager;
import de.fu_berlin.inf.dpp.videosharing.net.ConnectionFactory;
import de.fu_berlin.inf.dpp.videosharing.player.VideoDisplay;
import de.fu_berlin.inf.dpp.videosharing.source.ImageSource;
import de.fu_berlin.inf.dpp.videosharing.source.Screen;

/**
 * <p>
 * Main class for setting up videosharing-sessions.
 * </p>
 * <p>
 * Currently there is only one session at a time between two {@link User}s
 * possible.
 * </p>
 * 
 * @author s-lau
 */
@Component(module = "net")
public class VideoSharing {

    private static Logger log = Logger.getLogger(VideoSharing.class);

    public static final String VIDEO_SHARING = "de.fu_berlin.inf.dpp.videosharing";

    protected VideoSharingService videoSharingService;

    @Inject
    protected StreamServiceManager streamServiceManager;
    @Inject
    protected SarosUI sarosUI;
    @Inject
    protected SarosSessionObservable sarosSessionObservable;
    @Inject
    protected VideoSessionObservable videoSharingSessionObservable;

    protected Saros saros;
    protected IPreferenceStore preferences;

    /**
     * Flag which prevents setting up a session several times
     */
    protected boolean requestingSession = false;

    public VideoSharing(Saros saros, VideoSharingService videoSharingService) {
        this.saros = saros;
        this.preferences = saros.getPreferenceStore();
        this.videoSharingService = videoSharingService;
        videoSharingService.setVideoSharing(this);
    }

    /**
     * Setup a session with given user. It might not be possible to create a
     * session when errors during initialization occur. In this case no
     * exception is thrown, only {@link #videoSharingSessionObservable} is not
     * set and the session will notify the user about this error.
     * 
     * @param to
     *            share our screen with this user
     * @throws SarosCancellationException
     *             <code>to</code> does not want to see our screen
     */
    public synchronized void startSharing(User to)
        throws SarosCancellationException {
        requestingSession = true;
        try {
            if (videoSharingSessionObservable.getValue() != null) {
                return;
            }
            ConnectionFactory connectionFactory = null;
            StreamSession streamSession = null;

            try {
                if (to.isLocal()) {
                    connectionFactory = new ConnectionFactory();
                    videoSharingSessionObservable
                        .setValue(new VideoSharingSession(connectionFactory, to));
                    return;
                }
                try {
                    streamSession = streamServiceManager.createSession(
                        videoSharingService, to, VideoSharingInit.create(this),
                        null);
                    connectionFactory = new ConnectionFactory(streamSession,
                        Mode.HOST);
                    videoSharingSessionObservable
                        .setValue(new VideoSharingSession(streamSession,
                            connectionFactory, to));
                } catch (InterruptedException e) {
                    log.error("Code not designed to be interrupted: ", e);
                    Thread.currentThread().interrupt();
                    return;
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof SarosCancellationException) {
                        SarosCancellationException cancellation = (SarosCancellationException) cause;
                        throw cancellation;
                    } else {
                        log.error("Unexpected Exception: ", cause);
                    }
                    return;
                } catch (IOException e) {
                    if (streamSession != null)
                        streamSession.dispose();
                } catch (ConnectionException e) {
                    throw new SarosCancellationException("Could not connect.");
                } catch (TimeoutException e) {
                    throw new SarosCancellationException("Request timed out.");
                }
            } catch (InitializationException e) {
                log.error("Could not setup videosharing-session: ", e);
            }
        } finally {
            requestingSession = false;
        }
    }

    /**
     * @return whether creation of a new session is possible
     */
    public synchronized boolean ready() {
        return videoSharingSessionObservable.getValue() == null
            && !requestingSession;
    }

    /**
     * Receive the screen of another user
     * 
     * @param streamSession
     *            established session
     */
    protected synchronized void startSharing(StreamSession streamSession) {
        ConnectionFactory connectionFactory = null;
        try {
            connectionFactory = new ConnectionFactory(streamSession,
                Mode.CLIENT);
            videoSharingSessionObservable.setValue(new VideoSharingSession(
                streamSession, connectionFactory, sarosSessionObservable
                    .getValue().getUser(streamSession.getRemoteJID())));
        } catch (IOException e) {
            log.error("Could not create session: ", e);
        }
    }

    /**
     * @return local video-player-view or <code>null</code> when it was not
     *         possible to create one
     */
    protected VideoPlayerView getVideoPlayer() {
        Callable<VideoPlayerView> getView = new Callable<VideoPlayerView>() {

            public VideoPlayerView call() throws Exception {
                sarosUI.createVideoPlayerView();
                IViewPart view = Utils.findView(VideoPlayerView.class.getName());
                if (view instanceof VideoPlayerView) {
                    VideoPlayerView playerView = (VideoPlayerView) view;
                    return playerView;
                } else {
                    log.error("Got unknown view as videoplayer: "
                        + view.getClass().getName());
                    return null;
                }
            }

        };

        try {
            return Utils.runSWTSync(getView);
        } catch (Exception e) {
            log.error("Received unexpected exception: ", e);
        }

        return null;
    }

    /* some getters for the base settings */

    /**
     * @return width of encoding from eclipses preferences
     */
    protected int getVideoWidth() {
        return preferences.getInt(PreferenceConstants.ENCODING_VIDEO_WIDTH);
    }

    /**
     * @return height of encoding from eclipses preferences
     */
    protected int getVideoHeight() {
        return preferences.getInt(PreferenceConstants.ENCODING_VIDEO_HEIGHT);
    }

    /**
     * @return encoders codec from eclipses preferences
     */
    protected Codec getCodec() {
        return Codec.valueOf(preferences
            .getString(PreferenceConstants.ENCODING_CODEC));
    }

    /**
     * Opens an error-dialog.
     * 
     * @param e
     *            caused to report this error. It's message is the text of the
     *            error.
     * @param title
     *            of the dialog
     */
    public static void reportErrorToUser(Exception e, final String title) {
        final String message = e.getMessage();

        Utils.runSafeSWTAsync(log, new Runnable() {
            public void run() {
                DialogUtils.openErrorMessageDialog(EditorAPI.getShell(),
                    title, message);
            }
        });

    }

    /**
     * Kind of {@link VideoSharingSession}.
     */
    public static enum Mode {
        /**
         * We are providing our screen
         */
        HOST,
        /**
         * We are receiving a screen
         */
        CLIENT,
        /**
         * Local testing ({@link #HOST} and {@link #CLIENT})
         */
        LOCAL
    }

    /**
     * Available codecs for transmission of images
     */
    public static enum Codec {
        XUGGLER("Xuggler (pure video, needs Xuggler installed, recommended)"), IMAGE(
            "Tile-Encoding (based on images, pure Java, extremely slow)");

        String humanName;

        private Codec(String name) {
            this.humanName = name;
        }

        public static String[][] getNamesAndValues() {
            String[][] namesAndValues = new String[Codec.values().length][];

            int i = 0;
            for (Codec codec : Codec.values()) {
                namesAndValues[i] = new String[] { codec.humanName,
                    codec.name() };
                ++i;
            }

            return namesAndValues;
        }

        public String getHumanName() {
            return this.humanName;
        }
    }

    public class VideoSharingSession implements Disposable {
        @SuppressWarnings("hiding")
        private Logger log = Logger.getLogger(VideoSharingSession.class);

        protected Mode mode;
        protected StreamSession streamSession;
        protected ConnectionFactory connectionFactory = null;
        protected VideoSharingInit init = null;
        protected boolean paused = false;
        protected boolean isDisposing = false;

        protected User remoteUser;

        /* host */

        protected Encoder encoder = null;
        protected ActivityManager activityManager = null;
        protected ConnectionManager connectionManager = null;
        protected Screen screen;

        /* client */

        protected Decoder decoder = null;
        protected VideoDisplay videoDisplay = null;
        protected Thread errorThread = null;

        /**
         * Setup and start a session.
         * 
         * @param streamSession
         *            established connection
         * @param connectionFactory
         *            properly configured {@link ConnectionFactory}
         * @throws IllegalArgumentException
         *             <code>connectionFactory</code> not properly configured
         * @throws InitializationException
         *             A component could not initialize, session can not be
         *             created
         */
        public VideoSharingSession(StreamSession streamSession,
            ConnectionFactory connectionFactory, User remoteUser)
            throws InitializationException {
            super();
            this.mode = connectionFactory.getMode();
            this.remoteUser = remoteUser;

            if (mode == null || mode == Mode.LOCAL)
                throw new IllegalArgumentException(
                    "Mode can't be local or null");

            this.connectionFactory = connectionFactory;
            this.streamSession = streamSession;
            streamSession.setListener(new VideoSharingStreamSessionListener(
                this));
            try {
                switch (mode) {
                case HOST:
                    startHost();
                    break;
                case CLIENT:
                    if (streamSession.getInitiationDescription() instanceof VideoSharingInit) {
                        VideoSharingInit init = (VideoSharingInit) streamSession
                            .getInitiationDescription();
                        this.init = init;
                    }
                    startClient();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown Mode "
                        + mode.name());
                }
            } catch (EncoderInitializationException e) {
                throw new InitializationException(
                    "Could not initialize encoder", e);
            } catch (DecoderInitializationException e) {
                throw new InitializationException(
                    "Could not initialize decoder", e);
            }
        }

        /**
         * Starts a local session
         * 
         * @param connectionFactory
         * @param local
         *            this local user
         * @throws InitializationException
         *             A component could not initialize, session can not be
         *             created
         */
        public VideoSharingSession(ConnectionFactory connectionFactory,
            User local) throws InitializationException {
            super();
            this.remoteUser = local;
            this.mode = Mode.LOCAL;
            this.connectionFactory = connectionFactory;

            try {
                startHost();
                this.init = VideoSharingInit.create(VideoSharing.this);
                startClient();
            } catch (EncoderInitializationException e) {
                throw new InitializationException(
                    "Could not initialize encoder", e);
            } catch (DecoderInitializationException e) {
                throw new InitializationException(
                    "Could not initialize decoder", e);
            }
        }

        /**
         * Creates hosts {@link Encoder}, {@link ActivityManager} and
         * {@link ConnectionManager}, and starts encoder.
         * 
         * @throws EncoderInitializationException
         *             encoder was not able to start
         */
        protected synchronized void startHost()
            throws EncoderInitializationException {
            if (isDisposing)
                return;
            screen = new Screen(this);

            try {
                encoder = Encoder.getEncoder(getCodec(),
                    connectionFactory.getVideoOutputStream(), screen, this);
            } catch (EncoderInitializationException e) {
                reportError(e);
                throw e;
            }

            activityManager = new ActivityManager(screen, this,
                connectionFactory.getActivitiesInputStream());

            connectionManager = new ConnectionManager(
                connectionFactory.getVideoOutputStream(), encoder,
                connectionFactory.getDecodingStatisticsInputStream(), this);

            encoder.startEncoding();
        }

        /**
         * Creates clients {@link Decoder} and {@link VideoDisplay}, and starts
         * the decoder.
         * 
         * @throws DecoderInitializationException
         *             decoder was not able to initialize
         */
        protected synchronized void startClient()
            throws DecoderInitializationException {
            if (isDisposing)
                return;
            try {
                decoder = Decoder.getDecoder(init.codec,
                    connectionFactory.getVideoInputStream(),
                    connectionFactory.getDecodeStaticticsOutputStream(),
                    init.width, init.height, init.encoderFormatName, this);
            } catch (DecoderInitializationException e) {
                reportError(e);
                throw e;
            }

            videoDisplay = getVideoPlayer();

            Utils.runSafeSWTAsync(log, new Runnable() {
                public void run() {
                    sarosUI.activateVideoPlayerView();
                }
            });

            videoDisplay.setActivityOutput(connectionFactory
                .getActivitiesOutputStream());

            decoder.addPlayer(videoDisplay);

            decoder.startDecoder();

            errorThread = Utils.runSafeAsync("VideoSharing-ErrorReceiver", log,
                new Runnable() {
                    public void run() {
                        try {
                            Object errorRaw = connectionFactory
                                .getClientErrorIn().readObject();
                            if (errorRaw instanceof SessionErrorVideoActivity) {
                                SessionErrorVideoActivity error = (SessionErrorVideoActivity) errorRaw;
                                if (!(error.getException() instanceof IOException))
                                    // IOE are just confusing
                                    VideoSharing.reportErrorToUser(
                                        error.getException(),
                                        "Screensharing: Inviter got an exception");
                            }
                        } catch (IOException e) {
                            // ignore
                        } catch (ClassNotFoundException e) {
                            // ignore
                        }
                    }
                });
        }

        public Mode getMode() {
            return mode;
        }

        public StreamSession getStreamSession() {
            return streamSession;
        }

        public ConnectionFactory getConnectionFactory() {
            return connectionFactory;
        }

        public Encoder getEncoder() throws IllegalStateException {
            if (mode != Mode.LOCAL && mode != Mode.HOST)
                throw new IllegalStateException("Only inviter has an encoder.");

            return encoder;
        }

        public ActivityManager getActivityManager()
            throws IllegalStateException {
            if (mode != Mode.LOCAL && mode != Mode.HOST)
                throw new IllegalStateException(
                    "Only inviter has an ActivityManger.");

            return activityManager;
        }

        public ConnectionManager getConnectionManager()
            throws IllegalStateException {
            if (mode != Mode.LOCAL && mode != Mode.HOST)
                throw new IllegalStateException(
                    "Only inviter has a ConnectionManager.");

            return connectionManager;
        }

        public Screen getScreen() throws IllegalStateException {
            if (mode != Mode.LOCAL && mode != Mode.HOST)
                throw new IllegalStateException("Only inviter has a screen.");

            return screen;
        }

        public Decoder getDecoder() throws IllegalStateException {
            if (mode != Mode.LOCAL && mode != Mode.CLIENT)
                throw new IllegalStateException("Only client has a decoder.");

            return decoder;
        }

        public VideoDisplay getVideoDisplay() throws IllegalStateException {
            if (mode != Mode.LOCAL && mode != Mode.CLIENT)
                throw new IllegalStateException(
                    "Only client has a VideoDisplay.");

            return videoDisplay;
        }

        /**
         * Stops this session and disposes all components.
         */
        public synchronized void dispose() {
            if (isDisposing)
                return;
            isDisposing = true;
            try {
                if (mode.equals(Mode.HOST) || mode.equals(Mode.LOCAL)) {
                    if (encoder != null && encoder.isEncoding()) {
                        encoder.stopEncoding();
                        try {
                            encoder.getEncoderThread().join(1000);
                            if (encoder.isEncoding())
                                encoder.getEncoderThread().interrupt();
                        } catch (InterruptedException e) {
                            log.warn("Not designed to be interrupted: ", e);
                            Thread.currentThread().interrupt();
                        }
                        encoder = null;
                    }
                    if (activityManager != null) {
                        activityManager.dispose();
                        activityManager = null;
                    }
                    if (connectionManager != null) {
                        connectionManager.dispose();
                        connectionManager = null;
                    }
                    if (screen != null) {
                        screen.dispose();
                        screen = null;
                    }
                }
                if (mode.equals(Mode.CLIENT) || mode.equals(Mode.LOCAL)) {
                    if (decoder != null) {
                        decoder.dispose();
                        decoder = null;
                    }
                    if (errorThread != null) {
                        errorThread.interrupt();
                        errorThread = null;
                    }
                }

                if (connectionFactory != null) {
                    connectionFactory.dispose();
                    connectionFactory = null;
                }

                if (streamSession != null)
                    streamSession.stopSession();
            } finally {
                videoSharingSessionObservable.setValue(null);
            }
        }

        /**
         * For client: Send a request to stop this session
         */
        public void requestStop() {
            try {
                connectionFactory.getActivitiesOutputStream().writeObject(
                    SessionVideoActivity.createSessionStopActivity());
            } catch (IOException e) {
                dispose();
            }
        }

        /**
         * For client: Send a request to change mode of {@link ImageSource}
         */
        public void requestChangeImageSourceMode() {
            try {
                connectionFactory.getActivitiesOutputStream().writeObject(
                    new ImageSourceSwitchModeVideoActivity());
            } catch (IOException e) {
                reportError(e);
            }
        }

        /**
         * Toggles pause-state
         */
        public void pause() {
            paused = !paused;

            switch (mode) {
            case LOCAL: //$FALL-THROUGH$
            case HOST:
                encoder.pause();
                break;
            case CLIENT:
                try {
                    connectionFactory.getActivitiesOutputStream().writeObject(
                        SessionVideoActivity.createSessionPauseActivity());
                } catch (IOException e) {
                    reportError(e);
                }
                break;
            }
        }

        public boolean isPaused() {
            return paused;
        }

        /**
         * Reports an error during the session, disposes it and reports to the
         * local and buddy. Only the first one in this session will be
         * reported, subsequent errors (which may occur while disposing) will be
         * ignored.
         * 
         * @param e
         */
        public synchronized void reportError(Exception e) {
            if (isDisposing)
                // one error is enough
                return;
            log.error("Got an error during session: ", e);

            if (!(e instanceof IOException))
                // IOE are just confusing
                VideoSharing.reportErrorToUser(e,
                    "Screensharing: An error occured");

            // inform the other user
            try {
                SessionErrorVideoActivity sessionError = new SessionErrorVideoActivity(
                    e);
                if (mode.equals(Mode.HOST) || mode.equals(Mode.LOCAL)) {

                    connectionFactory.getHostErrorOut().writeObject(
                        sessionError);
                }
                if (mode.equals(Mode.CLIENT) || mode.equals(Mode.LOCAL)) {
                    connectionFactory.getActivitiesOutputStream().writeObject(
                        sessionError);
                }

            } catch (IOException e1) {
                // ignore, we tried
            }
            dispose();
        }

        public User getRemoteUser() {
            return remoteUser;
        }

    }

    public class VideoSharingStreamSessionListener implements
        StreamSessionListener {

        protected VideoSharingSession videoSharingSession;

        public VideoSharingStreamSessionListener(
            VideoSharingSession videoSharingSession) {
            this.videoSharingSession = videoSharingSession;
        }

        public void sessionStopped() {
            videoSharingSession.dispose();
            videoSharingSession.streamSession.shutdownFinished();
        }

        public void errorOccured(StreamException e) {
            videoSharingSession.reportError(e);
        }
    }

    /**
     * Value-object for synchronizing clients with hosts configuration
     */
    static class VideoSharingInit implements Serializable {
        private static final long serialVersionUID = -6857755592814263260L;

        int width;
        int height;
        Codec codec;
        String encoderFormatName;

        public VideoSharingInit(int width, int height, Codec codec,
            String encoderFormatName) {
            super();
            this.width = width;
            this.height = height;
            this.codec = codec;
            this.encoderFormatName = encoderFormatName;
        }

        /**
         * @param instance
         *            hosts instance
         * @return snapshot of hosts configuration
         */
        public static VideoSharingInit create(VideoSharing instance) {
            String encoderFormatName;

            switch (instance.getCodec()) {
            case XUGGLER:
                encoderFormatName = instance.preferences
                    .getString(PreferenceConstants.XUGGLER_CONTAINER_FORMAT);
                break;
            case IMAGE:
                encoderFormatName = instance.preferences
                    .getString(PreferenceConstants.IMAGE_TILE_CODEC);
                break;
            default:
                encoderFormatName = "";
            }
            return new VideoSharingInit(instance.getVideoWidth(),
                instance.getVideoHeight(), instance.getCodec(),
                encoderFormatName);
        }

    }

}
