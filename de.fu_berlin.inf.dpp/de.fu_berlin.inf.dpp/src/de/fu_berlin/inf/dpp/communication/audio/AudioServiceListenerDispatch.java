package de.fu_berlin.inf.dpp.communication.audio;

import java.util.ArrayList;
import java.util.List;

import de.fu_berlin.inf.dpp.net.internal.StreamSession;

public class AudioServiceListenerDispatch implements IAudioServiceListener {

    protected List<IAudioServiceListener> audioListeners = new ArrayList<IAudioServiceListener>();

    public void add(IAudioServiceListener audioListener) {
        if (!this.audioListeners.contains(audioListener)) {
            this.audioListeners.add(audioListener);
        }
    }

    public void remove(IAudioServiceListener audioListener) {
        this.audioListeners.remove(audioListener);
    }

    public synchronized void startSession(StreamSession newSession) {
        for (IAudioServiceListener listener : audioListeners) {
            listener.startSession(newSession);
        }
    }

    public void stopSession(StreamSession session) {
        for (IAudioServiceListener listener : audioListeners) {
            listener.stopSession(session);
        }
    }

    public void sessionStopped(StreamSession session) {
        for (IAudioServiceListener listener : audioListeners) {
            listener.sessionStopped(session);
        }
    }
}
