package de.fu_berlin.inf.dpp.ui.sounds;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;

import de.fu_berlin.inf.dpp.Saros;

public class SoundManager {

    private static final Logger log = Logger.getLogger(SoundManager.class);

    private static final String SOUND_DIR = "/assets/sounds/";

    public static final String USER_ONLINE = "UserComesOnline.wav";
    public static final String USER_OFFLINE = "UserGoesOffline.wav";
    public static final String MESSAGE_RECEIVED = "MessageReceived.wav";
    public static final String MESSAGE_SENT = "MessageSent.wav";

    /**
     * Returns a file from path
     */
    public static File getSoundFile(String filename) {
        URL url = Platform.getBundle(Saros.SAROS)
            .getEntry(SOUND_DIR + filename);
        try {
            url = FileLocator.toFileURL(url);
        } catch (IOException e1) {
            log.debug("could not convert to file-url", e1);
            return null;
        }
        URI uri = null;
        try {
            uri = url.toURI();
        } catch (URISyntaxException e) {
            log.debug("could not cast url to uri", e);
            return null;
        }
        File file = new File(uri);
        return file;
    }

}
