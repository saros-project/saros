package saros.ui.sounds;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.picocontainer.annotations.Inject;
import saros.Saros;
import saros.SarosPluginContext;
import saros.preferences.EclipsePreferenceConstants;
import saros.util.ThreadUtils;

public class SoundPlayer {

  private static final Logger log = Logger.getLogger(SoundPlayer.class.getName());

  private static final String SOUND_DIR = "/assets/sounds/";

  @Inject private static IPreferenceStore store;

  static {
    SarosPluginContext.initComponent(new SoundPlayer());
  }

  public static void playSound(final String filename) {

    if (store == null || !store.getBoolean(EclipsePreferenceConstants.SOUND_ENABLED)) {
      return;
    }

    ThreadUtils.runSafeAsync(
        "dpp-sound",
        log,
        new Runnable() {
          @Override
          public void run() {
            saros.misc.sound.SoundPlayer.playSound(getSoundFile(filename));
          }
        });
  }

  /** Returns a file from path */
  private static File getSoundFile(String filename) {
    URL url = Platform.getBundle(Saros.PLUGIN_ID).getEntry(SOUND_DIR + filename);
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
