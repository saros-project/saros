package de.fu_berlin.inf.dpp.observables;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.util.ObservableValue;
import de.fu_berlin.inf.dpp.videosharing.VideoSharing.VideoSharingSession;

@Component(module = "observables")
public class VideoSessionObservable extends
    ObservableValue<VideoSharingSession> {

    public VideoSessionObservable() {
        super(null);
    }

}
