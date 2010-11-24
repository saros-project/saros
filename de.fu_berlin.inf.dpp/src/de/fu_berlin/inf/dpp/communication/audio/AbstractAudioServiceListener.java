package de.fu_berlin.inf.dpp.communication.audio;

import de.fu_berlin.inf.dpp.net.internal.StreamSession;

public abstract class AbstractAudioServiceListener implements
    IAudioServiceListener {

    public synchronized void startSession(StreamSession newSession) {
        // nothing to do here
    }

    public void stopSession(StreamSession session) {
        // nothing to do here
    }

    public void sessionStopped(StreamSession session) {
        // nothing to do here
    }

}
