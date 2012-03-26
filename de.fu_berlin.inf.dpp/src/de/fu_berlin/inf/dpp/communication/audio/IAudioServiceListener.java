package de.fu_berlin.inf.dpp.communication.audio;

import de.fu_berlin.inf.dpp.net.internal.StreamSession;

public interface IAudioServiceListener {

    public void startSession(StreamSession newSession);

    public void stopSession(StreamSession session);

    public void sessionStopped(StreamSession session);

}
