package de.fu_berlin.inf.dpp.ui.sounds;

import java.io.File;
import java.io.IOException;
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
            log.debug("Could not convert to file URL:", e1);
            return null;
        }

        File file = new File(url.getFile());
        return file;
    }
}
